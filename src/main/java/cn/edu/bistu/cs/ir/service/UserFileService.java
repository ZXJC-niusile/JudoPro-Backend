package cn.edu.bistu.cs.ir.service;

import cn.edu.bistu.cs.ir.entity.UserFile;
import cn.edu.bistu.cs.ir.entity.AIAnalysis;
import cn.edu.bistu.cs.ir.repository.UserFileRepository;
import cn.edu.bistu.cs.ir.repository.AIAnalysisRepository;
import cn.edu.bistu.cs.ir.utils.FileUploadUtils;
import cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户文件服务类
 */
@Service
@Transactional
public class UserFileService {
    
    private static final Logger log = LoggerFactory.getLogger(UserFileService.class);
    
    @Autowired
    private UserFileRepository userFileRepository;
    
    @Autowired
    private FileUploadUtils fileUploadUtils;
    
    /**
     * 保存上传的图片文件信息
     */
    public UserFile saveImageFile(Long userId, MultipartFile file, String fileUrl) {
        return saveFileInfo(userId, file, fileUrl, "image");
    }
    
    /**
     * 保存上传的视频文件信息
     */
    public UserFile saveVideoFile(Long userId, MultipartFile file, String fileUrl) {
        return saveFileInfo(userId, file, fileUrl, "video");
    }
    
    /**
     * 保存文件信息到数据库
     */
    private UserFile saveFileInfo(Long userId, MultipartFile file, String fileUrl, String fileType) {
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        String storedFilename = extractStoredFilename(fileUrl);
        
        UserFile userFile = new UserFile(
            userId,
            originalFilename,
            storedFilename,
            fileUrl,
            fileType,
            extension,
            file.getSize(),
            file.getContentType()
        );
        
        UserFile savedFile = userFileRepository.save(userFile);
        log.info("保存文件信息到数据库：用户ID={}, 文件ID={}, 文件名={}", 
                userId, savedFile.getId(), originalFilename);
        
        return savedFile;
    }
    
    /**
     * 从文件URL中提取存储的文件名
     */
    private String extractStoredFilename(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "";
        }
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        return lastSlashIndex >= 0 ? fileUrl.substring(lastSlashIndex + 1) : fileUrl;
    }
    
    /**
     * 获取用户的文件列表（分页）
     */
    public Page<UserFile> getUserFiles(Long userId, Pageable pageable) {
        return userFileRepository.findByUserIdAndIsDeletedFalseOrderByUploadTimeDesc(userId, pageable);
    }
    
    /**
     * 根据文件类型获取用户的文件列表（分页）
     */
    public Page<UserFile> getUserFilesByType(Long userId, String fileType, Pageable pageable) {
        return userFileRepository.findByUserIdAndFileTypeAndIsDeletedFalseOrderByUploadTimeDesc(
                userId, fileType, pageable);
    }
    
    /**
     * 根据文件名搜索用户的文件
     */
    public Page<UserFile> searchUserFiles(Long userId, String filename, Pageable pageable) {
        return userFileRepository.findByUserIdAndOriginalFilenameContainingIgnoreCaseAndIsDeletedFalseOrderByUploadTimeDesc(
                userId, filename, pageable);
    }
    
    /**
     * 获取用户的文件详情
     */
    public Optional<UserFile> getUserFile(Long userId, Long fileId) {
        return userFileRepository.findByIdAndUserIdAndIsDeletedFalse(fileId, userId);
    }
    
    /**
     * 根据文件URL获取文件信息
     */
    public Optional<UserFile> getFileByUrl(String fileUrl) {
        return userFileRepository.findByFileUrlAndIsDeletedFalse(fileUrl);
    }
    
    /**
     * 获取用户最近上传的文件
     */
    public List<UserFile> getRecentFiles(Long userId) {
        return userFileRepository.findTop10ByUserIdAndIsDeletedFalseOrderByUploadTimeDesc(userId);
    }
    
    /**
     * 获取用户最常下载的文件
     */
    public List<UserFile> getPopularFiles(Long userId) {
        return userFileRepository.findTop10ByUserIdAndIsDeletedFalseOrderByDownloadCountDescUploadTimeDesc(userId);
    }
    
    /**
     * 增加文件下载次数
     */
    public boolean incrementDownloadCount(Long fileId) {
        try {
            int updated = userFileRepository.incrementDownloadCount(fileId, LocalDateTime.now());
            return updated > 0;
        } catch (Exception e) {
            log.error("更新文件下载次数失败：fileId={}", fileId, e);
            return false;
        }
    }
    
    /**
     * 删除用户文件（软删除）
     */
    public boolean deleteUserFile(Long userId, Long fileId) {
        try {
            Optional<UserFile> fileOpt = userFileRepository.findByIdAndUserIdAndIsDeletedFalse(fileId, userId);
            if (fileOpt.isPresent()) {
                UserFile userFile = fileOpt.get();
                
                // 删除物理文件
                boolean fileDeleted = fileUploadUtils.deleteFile(userFile.getFileUrl());
                
                // 软删除数据库记录
                int updated = userFileRepository.softDeleteByIdAndUserId(fileId, userId, LocalDateTime.now());
                
                if (updated > 0) {
                    log.info("删除用户文件成功：用户ID={}, 文件ID={}, 物理文件删除={}", 
                            userId, fileId, fileDeleted);
                    return true;
                } else {
                    log.warn("软删除文件记录失败：用户ID={}, 文件ID={}", userId, fileId);
                    return false;
                }
            } else {
                log.warn("文件不存在或无权限删除：用户ID={}, 文件ID={}", userId, fileId);
                return false;
            }
        } catch (Exception e) {
            log.error("删除用户文件失败：用户ID={}, 文件ID={}", userId, fileId, e);
            return false;
        }
    }
    
    /**
     * 根据文件URL删除用户文件
     */
    public boolean deleteUserFileByUrl(Long userId, String fileUrl) {
        try {
            Optional<UserFile> fileOpt = userFileRepository.findByUserIdAndFileUrlAndIsDeletedFalse(userId, fileUrl);
            if (fileOpt.isPresent()) {
                return deleteUserFile(userId, fileOpt.get().getId());
            } else {
                log.warn("文件不存在或无权限删除：用户ID={}, 文件URL={}", userId, fileUrl);
                return false;
            }
        } catch (Exception e) {
            log.error("根据URL删除用户文件失败：用户ID={}, 文件URL={}", userId, fileUrl, e);
            return false;
        }
    }
    
    /**
     * 获取用户文件统计信息
     */
    public UserFileStatistics getUserFileStatistics(Long userId) {
        long totalFiles = userFileRepository.countByUserIdAndIsDeletedFalse(userId);
        long totalImages = userFileRepository.countByUserIdAndFileTypeAndIsDeletedFalse(userId, "image");
        long totalVideos = userFileRepository.countByUserIdAndFileTypeAndIsDeletedFalse(userId, "video");
        long totalSize = userFileRepository.sumFileSizeByUserIdAndIsDeletedFalse(userId);
        
        return new UserFileStatistics(totalFiles, totalImages, totalVideos, totalSize);
    }
    
    /**
     * 更新文件描述
     */
    public boolean updateFileDescription(Long userId, Long fileId, String description) {
        try {
            Optional<UserFile> fileOpt = userFileRepository.findByIdAndUserIdAndIsDeletedFalse(fileId, userId);
            if (fileOpt.isPresent()) {
                UserFile userFile = fileOpt.get();
                userFile.setDescription(description);
                userFileRepository.save(userFile);
                log.info("更新文件描述成功：用户ID={}, 文件ID={}", userId, fileId);
                return true;
            } else {
                log.warn("文件不存在或无权限修改：用户ID={}, 文件ID={}", userId, fileId);
                return false;
            }
        } catch (Exception e) {
            log.error("更新文件描述失败：用户ID={}, 文件ID={}", userId, fileId, e);
            return false;
        }
    }
    
    /**
     * 用户文件统计信息类
     */
    public static class UserFileStatistics {
        private final long totalFiles;
        private final long totalImages;
        private final long totalVideos;
        private final long totalSize;
        
        public UserFileStatistics(long totalFiles, long totalImages, long totalVideos, long totalSize) {
            this.totalFiles = totalFiles;
            this.totalImages = totalImages;
            this.totalVideos = totalVideos;
            this.totalSize = totalSize;
        }
        
        public long getTotalFiles() { return totalFiles; }
        public long getTotalImages() { return totalImages; }
        public long getTotalVideos() { return totalVideos; }
        public long getTotalSize() { return totalSize; }
        
        public String getFormattedTotalSize() {
            if (totalSize < 1024) {
                return totalSize + " B";
            } else if (totalSize < 1024 * 1024) {
                return String.format("%.1f KB", totalSize / 1024.0);
            } else {
                return String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
            }
        }
    }

    // ===== 新增：支持AI分析结果的文件列表查询方法 =====

    @Autowired
    private AIAnalysisRepository aiAnalysisRepository;

    @Autowired
    private AIAnalysisService aiAnalysisService;

    /**
     * 获取用户文件和AI分析结果的合并列表
     * 同时返回原始文件和AI分析结果，用于个人中心展示
     */
    public java.util.List<cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO> getUserFilesWithAnalysis(
            Long userId,
            org.springframework.data.domain.Pageable pageable) {

        log.info("🔍 [文件列表] 开始获取用户{}的文件和AI分析结果", userId);

        java.util.List<cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO> result = new java.util.ArrayList<>();

        // 1. 获取原始文件列表
        log.info("📁 [文件列表] 查询原始文件列表...");
        org.springframework.data.domain.Page<UserFile> userFiles = userFileRepository.findByUserIdAndIsDeletedFalseOrderByUploadTimeDesc(userId, pageable);

        // 2. 获取AI分析结果列表
        log.info("🤖 [文件列表] 查询AI分析结果列表...");
        java.util.List<AIAnalysis> analysisList = aiAnalysisRepository.findByUserIdAndAnalysisStatusOrderByCreatedTimeDesc(userId, "completed");

        log.info("📊 [文件列表] 查询结果 - 原始文件数量: {}, AI分析结果数量: {}",
                userFiles.getTotalElements(), analysisList.size());

        // 3. 处理原始文件
        for (UserFile userFile : userFiles.getContent()) {
            cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO dto = cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO.fromUserFile(userFile);

            // 检查是否有对应的AI分析结果
            java.util.Optional<AIAnalysis> analysis = analysisList.stream()
                    .filter(a -> a.getUserFileId().equals(userFile.getId()))
                    .findFirst();

            if (analysis.isPresent()) {
                dto.addAnalysisInfo(analysis.get());
                log.info("✅ [文件列表] 文件ID {} 关联AI分析结果ID {}", userFile.getId(), analysis.get().getId());
            }

            result.add(dto);
        }

        // 4. 添加独立的AI分析结果（作为单独的卡片显示）
        for (AIAnalysis analysis : analysisList) {
            // 检查是否已经在原始文件中处理过
            boolean alreadyProcessed = result.stream()
                    .anyMatch(dto -> dto.getAnalysisId() != null && dto.getAnalysisId().equals(analysis.getId()));

            if (!alreadyProcessed) {
                // 获取原始文件信息
                java.util.Optional<UserFile> originalFile = userFileRepository.findById(analysis.getUserFileId());
                if (originalFile.isPresent()) {
                    cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO analysisDto = cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO
                            .fromAnalysisOnly(analysis, originalFile.get());
                    result.add(analysisDto);
                    log.info("🎯 [文件列表] 添加独立的AI分析结果卡片 - 分析ID: {}, 原文件ID: {}",
                            analysis.getId(), analysis.getUserFileId());
                }
            }
        }

        log.info("🎉 [文件列表] 合并完成 - 总返回数量: {}", result.size());

        return result;
    }

    /**
     * 获取用户文件和AI分析结果的分页合并列表（支持过滤）
     */
    public java.util.Map<String, Object> getUserFilesWithAnalysisPaged(
            Long userId,
            int page,
            int size,
            String type,
            String filename) {

        log.info("📄 [文件列表] 分页查询用户{}的文件和AI分析结果 - 页码: {}, 大小: {}, 类型: {}, 文件名: {}",
                userId, page, size, type, filename);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        java.util.List<cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO> allResults;
        long totalCount;

        if (type != null && !type.isEmpty()) {
            // 按类型过滤：只查询原始文件，然后添加对应的AI分析结果
            org.springframework.data.domain.Page<UserFile> userFiles = userFileRepository.findByUserIdAndFileTypeAndIsDeletedFalseOrderByUploadTimeDesc(userId, type, pageable);

            allResults = new java.util.ArrayList<>();
            totalCount = userFiles.getTotalElements();

            for (UserFile userFile : userFiles.getContent()) {
                cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO dto = cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO.fromUserFile(userFile);

                // 查找对应的AI分析结果
                java.util.Optional<AIAnalysis> analysis = aiAnalysisRepository.findByUserFileIdAndAnalysisStatusOrderByCreatedTimeDesc(userFile.getId(), "completed").stream().findFirst();
                if (analysis.isPresent()) {
                    dto.addAnalysisInfo(analysis.get());
                }

                allResults.add(dto);
            }
        } else if (filename != null && !filename.isEmpty()) {
            // 按文件名过滤
            org.springframework.data.domain.Page<UserFile> userFiles = userFileRepository.findByUserIdAndOriginalFilenameContainingIgnoreCaseAndIsDeletedFalseOrderByUploadTimeDesc(userId, filename, pageable);

            allResults = new java.util.ArrayList<>();
            totalCount = userFiles.getTotalElements();

            for (UserFile userFile : userFiles.getContent()) {
                cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO dto = cn.edu.bistu.cs.ir.dto.FileWithAnalysisDTO.fromUserFile(userFile);

                java.util.Optional<AIAnalysis> analysis = aiAnalysisRepository.findByUserFileIdAndAnalysisStatusOrderByCreatedTimeDesc(userFile.getId(), "completed").stream().findFirst();
                if (analysis.isPresent()) {
                    dto.addAnalysisInfo(analysis.get());
                }

                allResults.add(dto);
            }
        } else {
            // 无过滤条件，获取所有文件和AI分析结果
            allResults = getUserFilesWithAnalysis(userId, pageable);

            // 计算总数（原始文件数 + 完成的AI分析结果数）
            long originalFileCount = userFileRepository.countByUserIdAndIsDeletedFalse(userId);
            long analysisCount = aiAnalysisRepository.countByUserIdAndAnalysisStatus(userId, "completed");
            totalCount = originalFileCount + analysisCount;
        }

        // 返回分页结果
        return java.util.Map.of(
            "files", allResults,
            "totalElements", totalCount,
            "totalPages", (int) Math.ceil((double) totalCount / size),
            "currentPage", page,
            "size", size,
            "hasNext", (page + 1) * size < totalCount,
            "hasPrevious", page > 0
        );
    }
}