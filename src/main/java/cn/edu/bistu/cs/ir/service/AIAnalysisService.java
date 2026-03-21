package cn.edu.bistu.cs.ir.service;

import cn.edu.bistu.cs.ir.entity.AIAnalysis;
import cn.edu.bistu.cs.ir.repository.AIAnalysisRepository;
import cn.edu.bistu.cs.ir.utils.FileUploadUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AI分析结果业务服务类
 */
@Service
@Transactional
public class AIAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AIAnalysisService.class);

    @Autowired
    private AIAnalysisRepository aiAnalysisRepository;

    @Autowired
    private FileUploadUtils fileUploadUtils;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建新的AI分析记录
     * @param userFileId 用户文件ID
     * @param userId 用户ID
     * @param mediaType 媒体类型
     * @param prompt 分析提示词
     * @return 创建的AI分析记录
     */
    public AIAnalysis createAnalysis(Long userFileId, Long userId, String mediaType, String prompt) {
        log.info("🔨 创建新的AI分析记录 - 用户文件ID: {}, 用户ID: {}, 媒体类型: {}",
                 userFileId, userId, mediaType);

        AIAnalysis analysis = new AIAnalysis(userFileId, userId, mediaType, prompt);

        try {
            AIAnalysis savedAnalysis = aiAnalysisRepository.save(analysis);
            log.info("✅ AI分析记录创建成功 - 分析ID: {}", savedAnalysis.getId());
            return savedAnalysis;
        } catch (Exception e) {
            log.error("❌ 创建AI分析记录失败", e);
            throw new RuntimeException("创建AI分析记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存AI分析结果
     * @param analysisId 分析记录ID
     * @param externalModelResult 外部模型结果
     * @param pythonServiceResult Python微服务结果JSON字符串（用于处理，不存储到数据库）
     * @param annotatedMediaUrl 标注媒体文件的URL（已由FileUploadController下载并保存）
     * @return 更新后的分析记录
     */
    public AIAnalysis saveAnalysisResults(Long analysisId, String externalModelResult,
                                        String pythonServiceResult, String annotatedMediaUrl) {
        log.info("💾 开始保存AI分析结果 - 分析ID: {}", analysisId);

        try {
            // 查找分析记录
            Optional<AIAnalysis> analysisOpt = aiAnalysisRepository.findById(analysisId);
            if (!analysisOpt.isPresent()) {
                log.error("❌ 未找到分析记录 - 分析ID: {}", analysisId);
                throw new IllegalArgumentException("未找到分析记录: " + analysisId);
            }

            AIAnalysis analysis = analysisOpt.get();
            log.info("🔍 找到分析记录 - 媒体类型: {}, 当前状态: {}", analysis.getMediaType(), analysis.getAnalysisStatus());

            // 保存文本描述结果为文件
            log.info("📝 开始保存文本描述结果为文件...");
            log.info("📄 外部模型结果长度: {}", externalModelResult != null ? externalModelResult.length() : 0);

            String textDescriptionUrl = null;
            String textDescriptionFilename = null;

            if (externalModelResult != null && externalModelResult.length() > 0) {
                log.info("📋 外部模型结果预览: {}", externalModelResult.length() > 100 ?
                        externalModelResult.substring(0, 100) + "..." : externalModelResult);

                // 使用FileUploadUtils保存文字描述为.txt文件
                try {
                    FileUploadUtils.AnnotatedFileResult textFileResult = fileUploadUtils.saveTextDescriptionAsFile(
                        externalModelResult, analysis.getMediaType()
                    );
                    textDescriptionUrl = textFileResult.getFileUrl();
                    textDescriptionFilename = textFileResult.getFilename();

                    log.info("✅ 文字描述文件保存成功: {}", textDescriptionUrl);
                    log.info("📁 文字描述文件名: {}", textDescriptionFilename);

                } catch (Exception e) {
                    log.error("❌ 保存文字描述文件失败: {}", e.getMessage(), e);
                    // 如果文件保存失败，仍然保存到数据库作为备份
                    analysis.setExternalModelResult(externalModelResult);
                    log.info("⚠️ 文字描述已保存到数据库作为备份");
                }
            }

            // 不将长文本存储到数据库，避免数据库膨胀
            analysis.setExternalModelResult(null);
            analysis.setExternalModelResultUrl(textDescriptionUrl);
            analysis.setExternalModelResultFilename(textDescriptionFilename);
            log.info("✅ 文字描述处理完成 - URL方式: {}", textDescriptionUrl != null);

            // 不存储Python微服务的Base64数据以节省数据库空间
            // 只处理标注文件，不存储原始JSON响应
            analysis.setPythonServiceResult(null);
            log.info("🗑️ 跳过Python微服务结果存储（避免Base64数据占用数据库空间）");
            log.debug("📊 Python微服务结果原始长度: {} 字符（已舍弃）", pythonServiceResult != null ? pythonServiceResult.length() : 0);

            // 处理标注文件URL
            if (annotatedMediaUrl != null && !annotatedMediaUrl.trim().isEmpty()) {
                log.info("🖼️ 开始处理标注文件URL更新");
                log.info("🔗 标注文件URL: {}", annotatedMediaUrl);

                // 从URL提取文件名
                String filename = extractFilenameFromUrl(annotatedMediaUrl);
                log.info("📁 提取的文件名: {}", filename);

                // 直接更新分析记录的标注文件信息
                analysis.setAnnotatedMediaUrl(annotatedMediaUrl);
                analysis.setAnnotatedFilename(filename);

                log.info("✅ 标注文件URL信息已更新到分析记录");
            } else {
                log.info("📝 无标注文件URL，仅保存文本结果");
            }

            // 标记分析完成
            analysis.markAsCompleted();

            // 保存更新
            AIAnalysis savedAnalysis = aiAnalysisRepository.save(analysis);

            log.info("✅ AI分析结果保存成功 - 分析ID: {}", savedAnalysis.getId());
            log.info("📄 外部模型结果长度: {} 字符",
                    externalModelResult != null ? externalModelResult.length() : 0);
            log.info("📄 外部模型结果是否保存: {}", savedAnalysis.getExternalModelResult() != null ? "✅" : "❌");
            if (savedAnalysis.getExternalModelResult() != null && savedAnalysis.getExternalModelResult().length() > 0) {
                log.info("📋 保存的文字描述预览: {}",
                    savedAnalysis.getExternalModelResult().length() > 100 ?
                    savedAnalysis.getExternalModelResult().substring(0, 100) + "..." :
                    savedAnalysis.getExternalModelResult());
            }
            log.info("🐍 Python微服务结果: {}（未存储，仅保留标注文件）",
                    pythonServiceResult != null ? "已处理" : "为空");
            log.info("🎯 标注文件URL: {}", savedAnalysis.getAnnotatedMediaUrl());

            return savedAnalysis;

        } catch (Exception e) {
            log.error("❌ 保存AI分析结果失败 - 分析ID: {}", analysisId, e);

            // 标记分析失败
            try {
                Optional<AIAnalysis> analysisOpt = aiAnalysisRepository.findById(analysisId);
                if (analysisOpt.isPresent()) {
                    analysisOpt.get().markAsFailed(e.getMessage());
                    aiAnalysisRepository.save(analysisOpt.get());
                }
            } catch (Exception markFailedEx) {
                log.error("❌ 标记分析失败时也出错", markFailedEx);
            }

            throw new RuntimeException("保存AI分析结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存标注媒体文件
     * @param analysis 分析记录
     * @param annotatedMediaData 标注媒体Base64数据
     */
    private void saveAnnotatedMediaFile(AIAnalysis analysis, String annotatedMediaData) {
        log.info("🔄 开始保存{}标注文件", analysis.getMediaType());

        try {
            // 从Python微服务结果中提取实际的Base64数据
            String actualBase64Data = extractBase64FromPythonResult(analysis.getMediaType(), annotatedMediaData);

            if (actualBase64Data != null && !actualBase64Data.trim().isEmpty()) {
                // 获取原始文件扩展名（假设与源文件相同）
                String originalExtension = getFileExtensionFromMediaType(analysis.getMediaType());

                // 保存标注文件
                FileUploadUtils.AnnotatedFileResult result = fileUploadUtils.saveAnnotatedFile(
                    actualBase64Data,
                    analysis.getMediaType(),
                    originalExtension
                );

                // 更新分析记录
                analysis.setAnnotatedMediaUrl(result.getFileUrl());
                analysis.setAnnotatedFilename(result.getFilename());

                log.info("✅ {}标注文件保存成功", analysis.getMediaType());
                log.info("📁 文件URL: {}", result.getFileUrl());
                log.info("📊 文件大小: {}", result.getFormattedFileSize());

            } else {
                log.warn("⚠️ 未找到有效的标注文件Base64数据");
            }

        } catch (Exception e) {
            log.error("❌ 保存{}标注文件失败", analysis.getMediaType(), e);
            throw new RuntimeException("保存标注文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从Python微服务结果中提取Base64数据
     * @param mediaType 媒体类型
     * @param pythonServiceResult Python微服务结果
     * @return Base64数据字符串
     */
    private String extractBase64FromPythonResult(String mediaType, String pythonServiceResult) {
        try {
            log.info("🔍 解析Python微服务结果，媒体类型: {}", mediaType);
            log.info("📝 Python结果预览: {}", pythonServiceResult.length() > 200 ?
                    pythonServiceResult.substring(0, 200) + "..." : pythonServiceResult);

            // 检查是否为JSON格式
            if (pythonServiceResult.trim().startsWith("{") || pythonServiceResult.trim().startsWith("[")) {
                log.info("📋 检测到JSON格式，尝试解析");
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(pythonServiceResult, Map.class);

                String base64Data = null;

                if ("image".equals(mediaType)) {
                    base64Data = (String) result.get("image_base64_data");
                    log.info("🖼️ 提取图片Base64数据: {}", base64Data != null ? "成功" : "失败");
                } else if ("video".equals(mediaType)) {
                    base64Data = (String) result.get("video_base64_data");
                    log.info("🎬 提取视频Base64数据: {}", base64Data != null ? "成功" : "失败");
                }

                return base64Data;
            } else {
                // 如果不是JSON格式，可能是直接返回的Base64数据
                log.info("📋 检测到非JSON格式，可能为直接的Base64数据");
                return pythonServiceResult;
            }

        } catch (Exception e) {
            log.error("❌ 解析Python微服务结果失败，返回原始数据", e);
            // 如果JSON解析失败，直接返回原始数据
            return pythonServiceResult;
        }
    }

    /**
     * 根据媒体类型获取文件扩展名
     * @param mediaType 媒体类型
     * @return 文件扩展名
     */
    private String getFileExtensionFromMediaType(String mediaType) {
        switch (mediaType) {
            case "image":
                return "jpg"; // 默认使用jpg格式
            case "video":
                return "mp4"; // 默认使用mp4格式
            default:
                return "jpg"; // 默认扩展名
        }
    }

    /**
     * 根据用户文件ID查找分析结果
     * @param userFileId 用户文件ID
     * @return 分析结果列表（最新的在前）
     */
    @Transactional(readOnly = true)
    public List<AIAnalysis> findByUserFileId(Long userFileId) {
        log.info("🔍 查找用户文件ID {} 的分析结果", userFileId);
        return aiAnalysisRepository.findByUserFileIdOrderByCreatedTimeDesc(userFileId);
    }

    /**
     * 根据用户文件ID查找最新的分析结果
     * @param userFileId 用户文件ID
     * @return 最新的分析结果
     */
    @Transactional(readOnly = true)
    public Optional<AIAnalysis> findLatestByUserFileId(Long userFileId) {
        log.info("🔍 查找用户文件ID {} 的最新分析结果", userFileId);
        return aiAnalysisRepository.findFirstByUserFileIdOrderByCreatedTimeDesc(userFileId);
    }

    /**
     * 根据用户ID查找所有分析结果
     * @param userId 用户ID
     * @return 分析结果列表
     */
    @Transactional(readOnly = true)
    public List<AIAnalysis> findByUserId(Long userId) {
        log.info("🔍 查找用户ID {} 的所有分析结果", userId);
        return aiAnalysisRepository.findByUserIdOrderByCreatedTimeDesc(userId);
    }

    /**
     * 根据分析ID查找分析结果
     * @param analysisId 分析ID
     * @return 分析结果
     */
    @Transactional(readOnly = true)
    public Optional<AIAnalysis> findById(Long analysisId) {
        log.info("🔍 查找分析ID {} 的结果", analysisId);
        return aiAnalysisRepository.findById(analysisId);
    }

    /**
     * 删除分析记录（包括关联的标注文件）
     * @param analysisId 分析ID
     */
    public void deleteAnalysis(Long analysisId) {
        log.info("🗑️ 开始删除分析记录 - 分析ID: {}", analysisId);

        try {
            Optional<AIAnalysis> analysisOpt = aiAnalysisRepository.findById(analysisId);
            if (!analysisOpt.isPresent()) {
                log.warn("⚠️ 未找到要删除的分析记录 - 分析ID: {}", analysisId);
                return;
            }

            AIAnalysis analysis = analysisOpt.get();

            // 删除关联的标注文件
            if (analysis.getAnnotatedMediaUrl() != null) {
                boolean deleted = fileUploadUtils.deleteFile(analysis.getAnnotatedMediaUrl());
                log.info("🗑️ 标注文件删除: {}", deleted ? "成功" : "失败");
            }

            // 删除数据库记录
            aiAnalysisRepository.deleteById(analysisId);

            log.info("✅ 分析记录删除成功 - 分析ID: {}", analysisId);

        } catch (Exception e) {
            log.error("❌ 删除分析记录失败 - 分析ID: {}", analysisId, e);
            throw new RuntimeException("删除分析记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 统计用户的分析结果数量
     * @param userId 用户ID
     * @return 分析结果数量
     */
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        long count = aiAnalysisRepository.countByUserId(userId);
        log.info("📊 用户ID {} 的分析结果总数: {}", userId, count);
        return count;
    }

    /**
     * 获取用户的分析状态统计
     * @param userId 用户ID
     * @return 按状态分组的统计结果
     */
    @Transactional(readOnly = true)
    public List<Object[]> getStatusStatisticsByUserId(Long userId) {
        log.info("📊 获取用户ID {} 的分析状态统计", userId);
        return aiAnalysisRepository.countByUserIdGroupByStatus(userId);
    }

    /**
     * 从URL提取文件名
     * @param url 文件URL
     * @return 文件名
     */
    private String extractFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "unknown";
        }

        // 移除查询参数
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            url = url.substring(0, queryIndex);
        }

        // 提取文件名
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < url.length() - 1) {
            String filename = url.substring(lastSlash + 1);
            log.info("📝 从URL提取文件名: {} -> {}", url, filename);
            return filename;
        }

        log.warn("⚠️ 无法从URL提取文件名: {}", url);
        return "unknown";
    }

    /**
     * 限制字符串长度，避免超出数据库字段限制
     * @param str 原始字符串
     * @param maxLength 最大长度
     * @return 限制长度后的字符串
     */
    private String limitStringLength(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
}