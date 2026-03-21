package cn.edu.bistu.cs.ir.dto;

import cn.edu.bistu.cs.ir.entity.UserFile;
import cn.edu.bistu.cs.ir.entity.AIAnalysis;

import java.time.LocalDateTime;

/**
 * 文件与AI分析结果合并的数据传输对象
 * 用于在文件列表中同时展示原始文件和AI分析结果
 */
public class FileWithAnalysisDTO {

    // ===== 原始文件信息 =====
    private Long fileId;
    private Long userId;
    private String originalFilename;
    private String storedFilename;
    private String fileUrl;
    private String fileType;
    private String fileExtension;
    private Long fileSize;
    private String mimeType;
    private String description;
    private Integer downloadCount;
    private LocalDateTime uploadTime;
    private LocalDateTime lastAccessTime;
    private Boolean isDeleted;
    private String formattedFileSize;

    // ===== AI分析结果信息 =====
    private Long analysisId;
    private String annotatedMediaUrl;      // 标点文件URL
    private String annotatedFilename;      // 标点文件名
    private String externalModelResultUrl; // 文字描述文件URL
    private String externalModelResultFilename; // 文字描述文件名
    private String externalModelResult;   // 文字描述内容
    private String analysisStatus;
    private LocalDateTime analysisTime;
    private String mediaType;

    // ===== 标记字段 =====
    private Boolean hasAnalysis;           // 是否有AI分析结果
    private String displayType;            // 显示类型：original 或 analysis

    // ===== 构造函数 =====
    public FileWithAnalysisDTO() {
        this.hasAnalysis = false;
        this.displayType = "original";
    }

    /**
     * 从UserFile创建原始文件DTO
     */
    public static FileWithAnalysisDTO fromUserFile(UserFile userFile) {
        FileWithAnalysisDTO dto = new FileWithAnalysisDTO();

        // 复制原始文件信息
        dto.setFileId(userFile.getId());
        dto.setUserId(userFile.getUserId());
        dto.setOriginalFilename(userFile.getOriginalFilename());
        dto.setStoredFilename(userFile.getStoredFilename());
        dto.setFileUrl(userFile.getFileUrl());
        dto.setFileType(userFile.getFileType());
        dto.setFileExtension(userFile.getFileExtension());
        dto.setFileSize(userFile.getFileSize());
        dto.setMimeType(userFile.getMimeType());
        dto.setDescription(userFile.getDescription());
        dto.setDownloadCount(userFile.getDownloadCount());
        dto.setUploadTime(userFile.getUploadTime());
        dto.setLastAccessTime(userFile.getLastAccessTime());
        dto.setIsDeleted(userFile.getIsDeleted());
        dto.setFormattedFileSize(userFile.getFormattedFileSize());

        return dto;
    }

    /**
     * 添加AI分析结果信息
     */
    public void addAnalysisInfo(AIAnalysis analysis) {
        if (analysis != null) {
            this.hasAnalysis = true;
            this.displayType = "analysis";

            this.analysisId = analysis.getId();
            this.annotatedMediaUrl = analysis.getAnnotatedMediaUrl();
            this.annotatedFilename = analysis.getAnnotatedFilename();
            this.externalModelResultUrl = analysis.getExternalModelResultUrl();
            this.externalModelResultFilename = analysis.getExternalModelResultFilename();
            this.externalModelResult = analysis.getExternalModelResult();
            this.analysisStatus = analysis.getAnalysisStatus();
            this.analysisTime = analysis.getAnalysisTime();
            this.mediaType = analysis.getMediaType();
        }
    }

    /**
     * 创建仅包含AI分析结果的DTO（用于显示分析结果卡片）
     */
    public static FileWithAnalysisDTO fromAnalysisOnly(AIAnalysis analysis, UserFile originalFile) {
        FileWithAnalysisDTO dto = new FileWithAnalysisDTO();

        // 复制原始文件基本信息（用于显示）
        dto.setFileId(originalFile.getId());
        dto.setUserId(originalFile.getUserId());
        dto.setOriginalFilename(originalFile.getOriginalFilename() + " (AI分析结果)");
        dto.setFileType(originalFile.getFileType());
        dto.setFileSize(originalFile.getFileSize());
        dto.setFormattedFileSize(originalFile.getFormattedFileSize());
        dto.setUploadTime(originalFile.getUploadTime());

        // 添加AI分析信息
        dto.addAnalysisInfo(analysis);

        // 使用标点文件作为封面
        if (analysis.getAnnotatedMediaUrl() != null) {
            dto.setFileUrl(analysis.getAnnotatedMediaUrl());
        } else {
            dto.setFileUrl(originalFile.getFileUrl());
        }

        return dto;
    }

    // ===== Getter和Setter方法 =====

    // 原始文件信息
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getStoredFilename() { return storedFilename; }
    public void setStoredFilename(String storedFilename) { this.storedFilename = storedFilename; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }

    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime = lastAccessTime; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public String getFormattedFileSize() { return formattedFileSize; }
    public void setFormattedFileSize(String formattedFileSize) { this.formattedFileSize = formattedFileSize; }

    // AI分析结果信息
    public Long getAnalysisId() { return analysisId; }
    public void setAnalysisId(Long analysisId) { this.analysisId = analysisId; }

    public String getAnnotatedMediaUrl() { return annotatedMediaUrl; }
    public void setAnnotatedMediaUrl(String annotatedMediaUrl) { this.annotatedMediaUrl = annotatedMediaUrl; }

    public String getAnnotatedFilename() { return annotatedFilename; }
    public void setAnnotatedFilename(String annotatedFilename) { this.annotatedFilename = annotatedFilename; }

    public String getExternalModelResultUrl() { return externalModelResultUrl; }
    public void setExternalModelResultUrl(String externalModelResultUrl) { this.externalModelResultUrl = externalModelResultUrl; }

    public String getExternalModelResultFilename() { return externalModelResultFilename; }
    public void setExternalModelResultFilename(String externalModelResultFilename) { this.externalModelResultFilename = externalModelResultFilename; }

    public String getExternalModelResult() { return externalModelResult; }
    public void setExternalModelResult(String externalModelResult) { this.externalModelResult = externalModelResult; }

    public String getAnalysisStatus() { return analysisStatus; }
    public void setAnalysisStatus(String analysisStatus) { this.analysisStatus = analysisStatus; }

    public LocalDateTime getAnalysisTime() { return analysisTime; }
    public void setAnalysisTime(LocalDateTime analysisTime) { this.analysisTime = analysisTime; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    // 标记字段
    public Boolean getHasAnalysis() { return hasAnalysis; }
    public void setHasAnalysis(Boolean hasAnalysis) { this.hasAnalysis = hasAnalysis; }

    public String getDisplayType() { return displayType; }
    public void setDisplayType(String displayType) { this.displayType = displayType; }
}