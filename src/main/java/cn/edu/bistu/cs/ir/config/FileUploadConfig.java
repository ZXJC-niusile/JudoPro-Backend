package cn.edu.bistu.cs.ir.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * 文件上传配置类
 */
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig implements WebMvcConfigurer {
    
    /**
     * 文件上传根目录
     */
    private String uploadPath = "uploads";
    
    /**
     * 最大文件大小 (50MB)
     */
    private long maxFileSize = 50 * 1024 * 1024;
    
    /**
     * 最大请求大小 (100MB)
     */
    private long maxRequestSize = 100 * 1024 * 1024;
    
    /**
     * 支持的图片格式
     */
    private String[] allowedImageTypes = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
    
    /**
     * 支持的视频格式
     */
    private String[] allowedVideoTypes = {"mp4", "avi", "mov", "wmv", "flv", "mkv", "webm"};
    
    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(maxRequestSize);
        resolver.setMaxUploadSizePerFile(maxFileSize);
        resolver.setDefaultEncoding("UTF-8");
        return resolver;
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径
        String uploadDir = getAbsoluteUploadPath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + File.separator);
    }
    
    /**
     * 获取绝对上传路径
     */
    public String getAbsoluteUploadPath() {
        return Paths.get(uploadPath).toAbsolutePath().toString();
    }
    
    /**
     * 获取图片上传路径
     */
    public String getImageUploadPath() {
        return getAbsoluteUploadPath() + File.separator + "images";
    }
    
    /**
     * 获取视频上传路径
     */
    public String getVideoUploadPath() {
        return getAbsoluteUploadPath() + File.separator + "videos";
    }
    
    // Getters and Setters
    public String getUploadPath() {
        return uploadPath;
    }
    
    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }
    
    public long getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public long getMaxRequestSize() {
        return maxRequestSize;
    }
    
    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }
    
    public String[] getAllowedImageTypes() {
        return allowedImageTypes;
    }
    
    public void setAllowedImageTypes(String[] allowedImageTypes) {
        this.allowedImageTypes = allowedImageTypes;
    }
    
    public String[] getAllowedVideoTypes() {
        return allowedVideoTypes;
    }
    
    public void setAllowedVideoTypes(String[] allowedVideoTypes) {
        this.allowedVideoTypes = allowedVideoTypes;
    }
}