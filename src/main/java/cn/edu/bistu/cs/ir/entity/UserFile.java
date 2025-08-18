package cn.edu.bistu.cs.ir.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户文件实体类
 */
@Entity
@Table(name = "user_files")
public class UserFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 原始文件名
     */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    
    /**
     * 存储文件名（系统生成的唯一文件名）
     */
    @Column(name = "stored_filename", nullable = false, length = 255)
    private String storedFilename;
    
    /**
     * 文件访问URL
     */
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;
    
    /**
     * 文件类型（image/video）
     */
    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;
    
    /**
     * 文件扩展名
     */
    @Column(name = "file_extension", nullable = false, length = 10)
    private String fileExtension;
    
    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    /**
     * MIME类型
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    /**
     * 文件描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 下载次数
     */
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;
    
    /**
     * 上传时间
     */
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;
    
    /**
     * 最后访问时间
     */
    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;
    
    /**
     * 是否已删除（软删除）
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    /**
     * 删除时间
     */
    @Column(name = "delete_time")
    private LocalDateTime deleteTime;
    
    // 构造函数
    public UserFile() {
        this.uploadTime = LocalDateTime.now();
    }
    
    public UserFile(Long userId, String originalFilename, String storedFilename, 
                   String fileUrl, String fileType, String fileExtension, 
                   Long fileSize, String mimeType) {
        this();
        this.userId = userId;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
    
    public String getStoredFilename() {
        return storedFilename;
    }
    
    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getFileExtension() {
        return fileExtension;
    }
    
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getDownloadCount() {
        return downloadCount;
    }
    
    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }
    
    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }
    
    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }
    
    /**
     * 获取格式化的文件大小
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 增加下载次数
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
        this.lastAccessTime = LocalDateTime.now();
    }
    
    /**
     * 软删除文件
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deleteTime = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "UserFile{" +
                "id=" + id +
                ", userId=" + userId +
                ", originalFilename='" + originalFilename + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", uploadTime=" + uploadTime +
                '}';
    }
}