package cn.edu.bistu.cs.ir.utils;

import cn.edu.bistu.cs.ir.config.FileUploadConfig;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Component
public class FileUploadUtils {
    
    private static final Logger log = LoggerFactory.getLogger(FileUploadUtils.class);
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    @PostConstruct
    public void init() {
        // 创建上传目录
        createDirectoryIfNotExists(fileUploadConfig.getAbsoluteUploadPath());
        createDirectoryIfNotExists(fileUploadConfig.getImageUploadPath());
        createDirectoryIfNotExists(fileUploadConfig.getVideoUploadPath());
    }
    
    /**
     * 上传图片文件
     */
    public String uploadImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        return saveFile(file, fileUploadConfig.getImageUploadPath(), "images");
    }
    
    /**
     * 上传视频文件
     */
    public String uploadVideo(MultipartFile file) throws IOException {
        validateVideoFile(file);
        return saveFile(file, fileUploadConfig.getVideoUploadPath(), "videos");
    }
    
    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("图片文件不能为空");
        }
        
        String extension = getFileExtension(file.getOriginalFilename());
        if (!isAllowedImageType(extension)) {
            throw new IllegalArgumentException("不支持的图片格式，支持的格式：" + 
                Arrays.toString(fileUploadConfig.getAllowedImageTypes()));
        }
        
        if (file.getSize() > fileUploadConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("图片文件大小超过限制：" + 
                (fileUploadConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }
    }
    
    /**
     * 验证视频文件
     */
    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("视频文件不能为空");
        }
        
        String extension = getFileExtension(file.getOriginalFilename());
        if (!isAllowedVideoType(extension)) {
            throw new IllegalArgumentException("不支持的视频格式，支持的格式：" + 
                Arrays.toString(fileUploadConfig.getAllowedVideoTypes()));
        }
        
        if (file.getSize() > fileUploadConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("视频文件大小超过限制：" + 
                (fileUploadConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }
    }
    
    /**
     * 保存文件
     */
    private String saveFile(MultipartFile file, String uploadPath, String type) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 生成唯一文件名：时间戳_UUID.扩展名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String newFilename = timestamp + "_" + uuid + "." + extension;
        
        // 创建文件路径
        Path filePath = Paths.get(uploadPath, newFilename);
        
        // 保存文件
        Files.copy(file.getInputStream(), filePath);
        
        // 返回访问URL
        String accessUrl = "/uploads/" + type + "/" + newFilename;
        log.info("文件上传成功：{} -> {}", originalFilename, accessUrl);
        
        return accessUrl;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        return FilenameUtils.getExtension(filename).toLowerCase();
    }
    
    /**
     * 检查是否为允许的图片类型
     */
    private boolean isAllowedImageType(String extension) {
        return Arrays.asList(fileUploadConfig.getAllowedImageTypes()).contains(extension);
    }
    
    /**
     * 检查是否为允许的视频类型
     */
    private boolean isAllowedVideoType(String extension) {
        return Arrays.asList(fileUploadConfig.getAllowedVideoTypes()).contains(extension);
    }
    
    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String path) {
        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("创建上传目录：{}", path);
            }
        } catch (IOException e) {
            log.error("创建上传目录失败：{}", path, e);
            throw new RuntimeException("创建上传目录失败", e);
        }
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
                return false;
            }
            
            // 从URL中提取文件路径
            String relativePath = fileUrl.substring("/uploads/".length());
            Path filePath = Paths.get(fileUploadConfig.getAbsoluteUploadPath(), relativePath);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("删除文件成功：{}", fileUrl);
                return true;
            }
        } catch (IOException e) {
            log.error("删除文件失败：{}", fileUrl, e);
        }
        return false;
    }
    
    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
                return null;
            }
            
            String relativePath = fileUrl.substring("/uploads/".length());
            Path filePath = Paths.get(fileUploadConfig.getAbsoluteUploadPath(), relativePath);
            
            if (Files.exists(filePath)) {
                File file = filePath.toFile();
                return new FileInfo(
                    file.getName(),
                    file.length(),
                    getFileExtension(file.getName()),
                    fileUrl
                );
            }
        } catch (Exception e) {
            log.error("获取文件信息失败：{}", fileUrl, e);
        }
        return null;
    }
    
    /**
     * 文件信息类
     */
    public static class FileInfo {
        private String filename;
        private long size;
        private String extension;
        private String url;
        
        public FileInfo(String filename, long size, String extension, String url) {
            this.filename = filename;
            this.size = size;
            this.extension = extension;
            this.url = url;
        }
        
        // Getters
        public String getFilename() { return filename; }
        public long getSize() { return size; }
        public String getExtension() { return extension; }
        public String getUrl() { return url; }
        
        public String getFormattedSize() {
            if (size < 1024) {
                return size + " B";
            } else if (size < 1024 * 1024) {
                return String.format("%.1f KB", size / 1024.0);
            } else {
                return String.format("%.1f MB", size / (1024.0 * 1024.0));
            }
        }
    }
}