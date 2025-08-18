package cn.edu.bistu.cs.ir.controller;

import cn.edu.bistu.cs.ir.entity.UserFile;
import cn.edu.bistu.cs.ir.model.User;
import cn.edu.bistu.cs.ir.service.UserFileService;
import cn.edu.bistu.cs.ir.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "*")
public class FileUploadController {
    
    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
    
    @Autowired
    private FileUploadUtils fileUploadUtils;
    
    @Autowired
    private UserFileService userFileService;
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("用户未登录");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return ((User) principal).getId();
        } else if (principal instanceof String) {
            // 如果是用户名，需要通过UserService查找用户ID
            // 这里暂时抛出异常，需要根据实际认证实现调整
            throw new IllegalStateException("无法获取用户ID，请检查认证配置");
        }
        
        throw new IllegalStateException("无效的用户认证信息");
    }
    
    /**
     * 上传图片
     */
    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 上传图片并保存到数据库
            UserFile userFile = userFileService.saveImageFile(userId, file);
            
            response.put("success", true);
            response.put("message", "图片上传成功");
            response.put("data", Map.of(
                "id", userFile.getId(),
                "url", userFile.getFileUrl(),
                "filename", userFile.getOriginalFilename(),
                "size", userFile.getFormattedFileSize(),
                "type", "image",
                "uploadTime", userFile.getUploadTime()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("图片上传参数错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("图片上传失败", e);
            response.put("success", false);
            response.put("message", "图片上传失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 上传视频
     */
    @PostMapping("/upload/video")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 上传视频并保存到数据库
            UserFile userFile = userFileService.saveVideoFile(userId, file);
            
            response.put("success", true);
            response.put("message", "视频上传成功");
            response.put("data", Map.of(
                "id", userFile.getId(),
                "url", userFile.getFileUrl(),
                "filename", userFile.getOriginalFilename(),
                "size", userFile.getFormattedFileSize(),
                "type", "video",
                "uploadTime", userFile.getUploadTime()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("视频上传参数错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("视频上传失败", e);
            response.put("success", false);
            response.put("message", "视频上传失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取文件信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getFileInfo(
            @RequestParam("url") String fileUrl) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            FileUploadUtils.FileInfo fileInfo = fileUploadUtils.getFileInfo(fileUrl);
            
            if (fileInfo != null) {
                response.put("success", true);
                response.put("data", Map.of(
                    "filename", fileInfo.getFilename(),
                    "size", fileInfo.getFormattedSize(),
                    "extension", fileInfo.getExtension(),
                    "url", fileInfo.getUrl()
                ));
            } else {
                response.put("success", false);
                response.put("message", "文件不存在");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取文件信息失败", e);
            response.put("success", false);
            response.put("message", "获取文件信息失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 删除文件（通过文件ID）
     */
    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 删除用户文件
            boolean deleted = userFileService.deleteUserFile(userId, fileId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "文件删除成功");
            } else {
                response.put("success", false);
                response.put("message", "文件删除失败，文件不存在或无权限删除");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("删除文件失败", e);
            response.put("success", false);
            response.put("message", "删除文件失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 删除文件（通过文件URL，兼容旧接口）
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteFileByUrl(
            @RequestParam("url") String fileUrl,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 通过URL删除用户文件
            boolean deleted = userFileService.deleteUserFileByUrl(userId, fileUrl);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "文件删除成功");
            } else {
                response.put("success", false);
                response.put("message", "文件删除失败，文件不存在或无权限删除");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("删除文件失败", e);
            response.put("success", false);
            response.put("message", "删除文件失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 批量上传图片
     */
    @PostMapping("/upload/images")
    public ResponseEntity<Map<String, Object>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            if (files == null || files.length == 0) {
                response.put("success", false);
                response.put("message", "请选择要上传的图片文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (files.length > 10) {
                response.put("success", false);
                response.put("message", "一次最多上传10个文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> results = new HashMap<>();
            int successCount = 0;
            
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                try {
                    UserFile userFile = userFileService.saveImageFile(userId, file);
                    
                    results.put("file_" + i, Map.of(
                        "success", true,
                        "id", userFile.getId(),
                        "url", userFile.getFileUrl(),
                        "filename", userFile.getOriginalFilename(),
                        "size", userFile.getFormattedFileSize()
                    ));
                    successCount++;
                    
                } catch (Exception e) {
                    results.put("file_" + i, Map.of(
                        "success", false,
                        "filename", file.getOriginalFilename(),
                        "error", e.getMessage()
                    ));
                }
            }
            
            response.put("success", true);
            response.put("message", String.format("批量上传完成，成功：%d，失败：%d", 
                successCount, files.length - successCount));
            response.put("data", results);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("批量上传图片失败", e);
            response.put("success", false);
            response.put("message", "批量上传失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取用户文件列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String filename,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);
            
            // 获取用户文件列表
            Page<UserFile> userFiles = userFileService.getUserFiles(userId, type, filename, pageable);
            
            response.put("success", true);
            response.put("data", Map.of(
                "files", userFiles.getContent(),
                "totalElements", userFiles.getTotalElements(),
                "totalPages", userFiles.getTotalPages(),
                "currentPage", userFiles.getNumber(),
                "size", userFiles.getSize(),
                "hasNext", userFiles.hasNext(),
                "hasPrevious", userFiles.hasPrevious()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("获取用户文件列表失败", e);
            response.put("success", false);
            response.put("message", "获取文件列表失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取文件详情
     */
    @GetMapping("/detail/{fileId}")
    public ResponseEntity<Map<String, Object>> getFileDetail(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 获取文件详情
            UserFile userFile = userFileService.getFileDetail(userId, fileId);
            
            if (userFile != null) {
                response.put("success", true);
                response.put("data", userFile);
            } else {
                response.put("success", false);
                response.put("message", "文件不存在或无权限访问");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("获取文件详情失败", e);
            response.put("success", false);
            response.put("message", "获取文件详情失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 下载文件（增加下载次数）
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Map<String, Object>> downloadFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 增加下载次数并获取文件信息
            UserFile userFile = userFileService.incrementDownloadCount(userId, fileId);
            
            if (userFile != null) {
                response.put("success", true);
                response.put("message", "文件下载成功");
                response.put("data", Map.of(
                    "url", userFile.getFileUrl(),
                    "filename", userFile.getOriginalFilename(),
                    "downloadCount", userFile.getDownloadCount()
                ));
            } else {
                response.put("success", false);
                response.put("message", "文件不存在或无权限下载");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("文件下载失败", e);
            response.put("success", false);
            response.put("message", "文件下载失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取用户文件统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserFileStatistics(
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 获取用户文件统计信息
            UserFileService.UserFileStatistics statistics = userFileService.getUserFileStatistics(userId);
            
            response.put("success", true);
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("获取用户文件统计失败", e);
            response.put("success", false);
            response.put("message", "获取统计信息失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取最近上传的文件
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentFiles(
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 获取最近上传的文件
            List<UserFile> recentFiles = userFileService.getRecentFiles(userId);
            
            response.put("success", true);
            response.put("data", recentFiles);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("获取最近文件失败", e);
            response.put("success", false);
            response.put("message", "获取最近文件失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取热门下载文件
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularFiles(
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 获取热门下载文件
            List<UserFile> popularFiles = userFileService.getPopularFiles(userId);
            
            response.put("success", true);
            response.put("data", popularFiles);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("获取热门文件失败", e);
            response.put("success", false);
            response.put("message", "获取热门文件失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 更新文件描述
     */
    @PutMapping("/description/{fileId}")
    public ResponseEntity<Map<String, Object>> updateFileDescription(
            @PathVariable Long fileId,
            @RequestParam String description,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication);
            
            // 更新文件描述
            boolean updated = userFileService.updateFileDescription(userId, fileId, description);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "文件描述更新成功");
            } else {
                response.put("success", false);
                response.put("message", "文件描述更新失败，文件不存在或无权限修改");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("用户认证错误：{}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("更新文件描述失败", e);
            response.put("success", false);
            response.put("message", "更新文件描述失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}