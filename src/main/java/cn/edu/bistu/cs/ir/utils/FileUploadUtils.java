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
        log.info("=== FileUploadUtils 初始化 ===");

        // 创建上传目录
        String uploadPath = fileUploadConfig.getAbsoluteUploadPath();
        String imagePath = fileUploadConfig.getImageUploadPath();
        String videoPath = fileUploadConfig.getVideoUploadPath();
        String annotatedImagePath = fileUploadConfig.getAnnotatedImagePath();
        String annotatedVideoPath = fileUploadConfig.getAnnotatedVideoPath();
        String textDescriptionsPath = fileUploadConfig.getTextDescriptionsPath();
        String imageAnalysisTextPath = fileUploadConfig.getImageAnalysisTextPath();
        String videoAnalysisTextPath = fileUploadConfig.getVideoAnalysisTextPath();

        log.info("文件上传根目录: {}", uploadPath);
        log.info("图片上传目录: {}", imagePath);
        log.info("视频上传目录: {}", videoPath);
        log.info("标注图片目录: {}", annotatedImagePath);
        log.info("标注视频目录: {}", annotatedVideoPath);
        log.info("文字描述专用目录: {}", textDescriptionsPath);
        log.info("图片分析文字描述目录: {}", imageAnalysisTextPath);
        log.info("视频分析文字描述目录: {}", videoAnalysisTextPath);

        createDirectoryIfNotExists(uploadPath);
        createDirectoryIfNotExists(imagePath);
        createDirectoryIfNotExists(videoPath);
        createDirectoryIfNotExists(annotatedImagePath);
        createDirectoryIfNotExists(annotatedVideoPath);
        createDirectoryIfNotExists(textDescriptionsPath);
        createDirectoryIfNotExists(imageAnalysisTextPath);
        createDirectoryIfNotExists(videoAnalysisTextPath);

        log.info("✅ 目录初始化完成（包含文字描述专用目录）");
    }
    
    /**
     * 上传图片文件
     */
    public String uploadImage(MultipartFile file) throws IOException {
        log.info("开始上传图片文件: {}", file.getOriginalFilename());
        log.info("目标存储目录: {}", fileUploadConfig.getImageUploadPath());

        validateImageFile(file);

        String fileUrl = saveFile(file, fileUploadConfig.getImageUploadPath(), "images");
        log.info("图片上传成功，访问URL: {}", fileUrl);

        return fileUrl;
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
     * 从Base64字符串保存标注文件
     * @param base64Data Base64编码的文件数据（可能包含MIME前缀）
     * @param mediaType 媒体类型（image/video）
     * @param originalExtension 原始文件扩展名
     * @return 文件访问URL和存储文件名信息
     * @throws IOException 文件保存异常
     */
    public AnnotatedFileResult saveAnnotatedFile(String base64Data, String mediaType, String originalExtension) throws IOException {
        log.info("🔄 开始保存{}标注文件，原始扩展名: {}", mediaType, originalExtension);

        try {
            // 移除MIME前缀（如果存在）
            String cleanBase64Data = extractBase64FromDataUrl(base64Data);
            log.info("📝 Base64数据清理完成，长度: {} 字符", cleanBase64Data.length());

            // Base64解码
            byte[] fileBytes = java.util.Base64.getDecoder().decode(cleanBase64Data);
            log.info("📦 Base64解码完成，文件大小: {} bytes", fileBytes.length);

            // 直接使用标注文件目录
            String annotatedPath;
            String urlType;
            if ("image".equals(mediaType)) {
                annotatedPath = fileUploadConfig.getAnnotatedImagePath();
                urlType = "annotated_images";
                log.info("🖼️  图片标注文件，存储路径: {}", annotatedPath);
            } else if ("video".equals(mediaType)) {
                annotatedPath = fileUploadConfig.getAnnotatedVideoPath();
                urlType = "annotated_videos";
                log.info("🎬 视频标注文件，存储路径: {}", annotatedPath);
            } else {
                throw new IllegalArgumentException("不支持的媒体类型: " + mediaType);
            }

            // 确保标注文件目录存在
            createDirectoryIfNotExists(annotatedPath);

            // 生成唯一文件名：annotated_时间戳_UUID.扩展名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String annotatedFilename = "annotated_" + timestamp + "_" + uuid + "." + originalExtension;

            // 保存文件
            Path filePath = Paths.get(annotatedPath, annotatedFilename);
            Files.write(filePath, fileBytes);

            // 生成访问URL
            String accessUrl = "/uploads/" + urlType + "/" + annotatedFilename;

            log.info("✅ {}标注文件保存成功", mediaType);
            log.info("📁 存储路径: {}", filePath.toAbsolutePath());
            log.info("🔗 访问URL: {}", accessUrl);
            log.info("📊 文件大小: {} bytes", fileBytes.length);

            return new AnnotatedFileResult(accessUrl, annotatedFilename, fileBytes.length);

        } catch (IllegalArgumentException e) {
            log.error("❌ Base64数据格式错误: {}", e.getMessage());
            throw new IllegalArgumentException("Base64数据格式错误: " + e.getMessage());
        } catch (IOException e) {
            log.error("❌ 保存{}标注文件失败: {}", mediaType, e.getMessage());
            throw new IOException("保存" + mediaType + "标注文件失败: " + e.getMessage());
        }
    }

    /**
     * 从Data URL格式中提取纯Base64数据
     * @param dataUrl 可能包含MIME前缀的Base64数据
     * @return 纯Base64数据
     */
    private String extractBase64FromDataUrl(String dataUrl) {
        if (dataUrl == null || dataUrl.isEmpty()) {
            throw new IllegalArgumentException("Base64数据不能为空");
        }

        // 检查是否包含MIME前缀（如: "data:image/jpeg;base64,"）
        if (dataUrl.contains(",")) {
            String[] parts = dataUrl.split(",", 2);
            if (parts.length == 2) {
                log.info("📋 检测到MIME前缀: {}", parts[0]);
                return parts[1];
            }
        }

        return dataUrl;
    }

    /**
     * 标注文件保存结果
     */
    public static class AnnotatedFileResult {
        private String fileUrl;
        private String filename;
        private long fileSize;

        public AnnotatedFileResult(String fileUrl, String filename, long fileSize) {
            this.fileUrl = fileUrl;
            this.filename = filename;
            this.fileSize = fileSize;
        }

        public String getFileUrl() { return fileUrl; }
        public String getFilename() { return filename; }
        public long getFileSize() { return fileSize; }

        public String getFormattedFileSize() {
            if (fileSize < 1024) {
                return fileSize + " B";
            } else if (fileSize < 1024 * 1024) {
                return String.format("%.1f KB", fileSize / 1024.0);
            } else {
                return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
            }
        }
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

    /**
     * 保存标注文件的字节数组版本
     * @param fileBytes 文件字节数组
     * @param mediaType 媒体类型
     * @param originalFilename 原始文件名
     * @return 标注文件结果
     * @throws IOException 保存异常
     */
    public AnnotatedFileResult saveAnnotatedFileFromBytes(byte[] fileBytes, String mediaType, String originalFilename) throws IOException {
        log.info("🔄 开始保存{}标注文件（字节数组版本），原始文件名: {}", mediaType, originalFilename);
        log.info("📊 接收到的数据大小: {} bytes", fileBytes.length);

        try {
            // 生成唯一的文件名，保留原始扩展名
            String originalExtension = getFileExtension(originalFilename);
            if (originalExtension == null || originalExtension.isEmpty()) {
                originalExtension = "jpg"; // 默认扩展名
            }
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String randomSuffix = String.format("%04d", new java.util.Random().nextInt(10000));
            String annotatedFilename = String.format("annotated_%s_%s.%s", timestamp, randomSuffix, originalExtension);

            // 选择正确的存储路径
            String annotatedPath;
            String urlType;
            if ("image".equals(mediaType)) {
                annotatedPath = fileUploadConfig.getAnnotatedImagePath();
                urlType = "annotated_images";
                log.info("🖼️  图片标注文件，存储路径: {}", annotatedPath);
            } else if ("video".equals(mediaType)) {
                annotatedPath = fileUploadConfig.getAnnotatedVideoPath();
                urlType = "annotated_videos";
                log.info("🎬 视频标注文件，存储路径: {}", annotatedPath);
            } else {
                log.error("❌ 不支持的媒体类型: {}", mediaType);
                throw new IllegalArgumentException("不支持的媒体类型: " + mediaType);
            }

            // 确保目录存在
            java.io.File dir = new java.io.File(annotatedPath);
            if (!dir.exists()) {
                dir.mkdirs();
                log.info("📁 创建目录: {}", annotatedPath);
            }

            // 保存文件
            Path filePath = Paths.get(annotatedPath, annotatedFilename);
            Files.write(filePath, fileBytes);

            // 生成访问URL
            String accessUrl = "/uploads/" + urlType + "/" + annotatedFilename;

            log.info("✅ {}标注文件保存成功", mediaType);
            log.info("📁 存储路径: {}", filePath.toAbsolutePath());
            log.info("🔗 访问URL: {}", accessUrl);
            log.info("📊 文件大小: {} bytes", fileBytes.length);

            return new AnnotatedFileResult(accessUrl, annotatedFilename, fileBytes.length);

        } catch (IOException e) {
            log.error("❌ 保存{}标注文件失败: {}", mediaType, e.getMessage());
            throw new IOException("保存标注文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存文字描述为文本文件
     * @param textContent 文字描述内容
     * @param mediaType 媒体类型
     * @return 文本文件结果
     * @throws IOException 保存异常
     */
    public AnnotatedFileResult saveTextDescriptionAsFile(String textContent, String mediaType) throws IOException {
        log.info("🔄 开始保存{}的文字描述为文本文件", mediaType);
        log.info("📊 文字描述长度: {} 字符", textContent != null ? textContent.length() : 0);

        try {
            if (textContent == null || textContent.trim().isEmpty()) {
                throw new IllegalArgumentException("文字描述内容为空");
            }

            // 生成唯一的文件名
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String randomSuffix = String.format("%04d", new java.util.Random().nextInt(10000));
            String textFilename = String.format("analysis_%s_%s.txt", timestamp, randomSuffix);

            // 选择存储路径 - 使用专用文字描述文件夹
            String textPath;
            String urlType;
            if ("image".equals(mediaType)) {
                textPath = fileUploadConfig.getImageAnalysisTextPath(); // 使用专用图片分析文字描述目录
                urlType = "text_descriptions/image_analysis";
                log.info("🖼️ 图片分析文字描述，存储路径: {}", textPath);
                log.info("📂 文件夹类型: 专用图片分析文字描述目录");
            } else if ("video".equals(mediaType)) {
                textPath = fileUploadConfig.getVideoAnalysisTextPath(); // 使用专用视频分析文字描述目录
                urlType = "text_descriptions/video_analysis";
                log.info("🎬 视频分析文字描述，存储路径: {}", textPath);
                log.info("📂 文件夹类型: 专用视频分析文字描述目录");
            } else {
                log.error("❌ 不支持的媒体类型: {}", mediaType);
                throw new IllegalArgumentException("不支持的媒体类型: " + mediaType);
            }

            // 确保目录存在
            java.io.File dir = new java.io.File(textPath);
            if (!dir.exists()) {
                dir.mkdirs();
                log.info("📁 创建目录: {}", textPath);
            }

            // 保存文字描述为UTF-8编码的文本文件
            Path filePath = Paths.get(textPath, textFilename);
            byte[] textBytes = textContent.getBytes("UTF-8");
            Files.write(filePath, textBytes);

            // 生成访问URL
            String accessUrl = "/uploads/" + urlType + "/" + textFilename;

            log.info("✅ 文字描述文件保存成功 - 使用专用文字描述目录");
            log.info("📁 存储路径: {}", filePath.toAbsolutePath());
            log.info("🔗 访问URL: {}", accessUrl);
            log.info("📊 文件大小: {} bytes", textBytes.length);
            log.info("🆕 文件夹结构: 文字描述文件已从混合目录迁移至专用目录");

            return new AnnotatedFileResult(accessUrl, textFilename, textBytes.length);

        } catch (IOException e) {
            log.error("❌ 保存{}文字描述文件失败: {}", mediaType, e.getMessage());
            throw new IOException("保存文字描述文件失败: " + e.getMessage(), e);
        }
    }
}