package cn.edu.bistu.cs.ir.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * AI分析结果实体类
 * 存储AI模型分析后的结果数据
 */
@Entity
@Table(name = "ai_analysis")
public class AIAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的用户文件ID
     */
    @Column(name = "user_file_id", nullable = false)
    private Long userFileId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 外部模型（火山引擎）返回的文字描述
     */
    @Column(name = "external_model_result", columnDefinition = "TEXT")
    private String externalModelResult;

    /**
     * 外部模型文字描述文件的URL
     */
    @Column(name = "external_model_result_url", length = 500)
    private String externalModelResultUrl;

    /**
     * 外部模型文字描述文件名
     */
    @Column(name = "external_model_result_filename", length = 255)
    private String externalModelResultFilename;

    /**
     * Python微服务返回的完整结果JSON
     */
    @Column(name = "python_service_result", columnDefinition = "TEXT")
    private String pythonServiceResult;

    /**
     * 标注后的媒体文件URL
     */
    @Column(name = "annotated_media_url", length = 500)
    private String annotatedMediaUrl;

    /**
     * 标注媒体文件的存储文件名
     */
    @Column(name = "annotated_filename", length = 255)
    private String annotatedFilename;

    /**
     * 分析状态：pending-进行中，completed-完成，failed-失败
     */
    @Column(name = "analysis_status", nullable = false, length = 50)
    private String analysisStatus = "pending";

    /**
     * 分析完成时间
     */
    @Column(name = "analysis_time")
    private LocalDateTime analysisTime;

    /**
     * 错误消息（如果分析失败）
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    /**
     * 媒体类型（image/video）
     */
    @Column(name = "media_type", nullable = false, length = 20)
    private String mediaType;

    /**
     * 分析提示词
     */
    @Column(name = "prompt", length = 1000)
    private String prompt;

    // 构造函数
    public AIAnalysis() {
        this.createdTime = LocalDateTime.now();
        this.analysisStatus = "pending";
    }

    public AIAnalysis(Long userFileId, Long userId, String mediaType, String prompt) {
        this();
        this.userFileId = userFileId;
        this.userId = userId;
        this.mediaType = mediaType;
        this.prompt = prompt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserFileId() {
        return userFileId;
    }

    public void setUserFileId(Long userFileId) {
        this.userFileId = userFileId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getExternalModelResult() {
        return externalModelResult;
    }

    public void setExternalModelResult(String externalModelResult) {
        this.externalModelResult = externalModelResult;
    }

    public String getExternalModelResultUrl() {
        return externalModelResultUrl;
    }

    public void setExternalModelResultUrl(String externalModelResultUrl) {
        this.externalModelResultUrl = externalModelResultUrl;
    }

    public String getExternalModelResultFilename() {
        return externalModelResultFilename;
    }

    public void setExternalModelResultFilename(String externalModelResultFilename) {
        this.externalModelResultFilename = externalModelResultFilename;
    }

    public String getPythonServiceResult() {
        return pythonServiceResult;
    }

    public void setPythonServiceResult(String pythonServiceResult) {
        this.pythonServiceResult = pythonServiceResult;
    }

    public String getAnnotatedMediaUrl() {
        return annotatedMediaUrl;
    }

    public void setAnnotatedMediaUrl(String annotatedMediaUrl) {
        this.annotatedMediaUrl = annotatedMediaUrl;
    }

    public String getAnnotatedFilename() {
        return annotatedFilename;
    }

    public void setAnnotatedFilename(String annotatedFilename) {
        this.annotatedFilename = annotatedFilename;
    }

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public LocalDateTime getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(LocalDateTime analysisTime) {
        this.analysisTime = analysisTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * 标记分析为成功完成
     */
    public void markAsCompleted() {
        this.analysisStatus = "completed";
        this.analysisTime = LocalDateTime.now();
    }

    /**
     * 标记分析为失败
     */
    public void markAsFailed(String errorMessage) {
        this.analysisStatus = "failed";
        this.analysisTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    /**
     * 检查分析是否成功
     */
    public boolean isCompleted() {
        return "completed".equals(analysisStatus);
    }

    /**
     * 检查分析是否失败
     */
    public boolean isFailed() {
        return "failed".equals(analysisStatus);
    }

    @Override
    public String toString() {
        return "AIAnalysis{" +
                "id=" + id +
                ", userFileId=" + userFileId +
                ", userId=" + userId +
                ", mediaType='" + mediaType + '\'' +
                ", analysisStatus='" + analysisStatus + '\'' +
                ", analysisTime=" + analysisTime +
                '}';
    }
}