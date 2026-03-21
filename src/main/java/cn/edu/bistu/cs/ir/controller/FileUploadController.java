package cn.edu.bistu.cs.ir.controller;

import cn.edu.bistu.cs.ir.entity.AIAnalysis;
import cn.edu.bistu.cs.ir.entity.UserFile;
import cn.edu.bistu.cs.ir.model.User;
import cn.edu.bistu.cs.ir.service.UserFileService;
import cn.edu.bistu.cs.ir.service.UserService;
import cn.edu.bistu.cs.ir.service.AIAnalysisService;
import cn.edu.bistu.cs.ir.utils.FileUploadUtils;
import cn.edu.bistu.cs.ir.config.VolcengineConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Optional;

import okhttp3.RequestBody;
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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    // 服务器地址配置常量
    private static final String SERVER_HOST = "10.199.201.199";
    private static final String SERVER_PORT = "8080";
    private static final String PYTHON_MICROSERVICE_PORT = "8000";

    // 微服务地址常量
    private static final String PYTHON_BINARY_URL = "http://" + SERVER_HOST + ":" + PYTHON_MICROSERVICE_PORT + "/analyze_binary";
    private static final String PYTHON_BASE64_URL = "http://" + SERVER_HOST + ":" + "5000" + "/analyze";
    
    @Autowired
    private FileUploadUtils fileUploadUtils;
    
    @Autowired
    private UserFileService userFileService;

    @Autowired
    private UserService userService;

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private VolcengineConfig volcengineConfig;

    /**
     * 获取当前登录用户ID - 支持Session认证和Spring Security认证
     */
    private Long getCurrentUserId(Authentication authentication, HttpServletRequest request) {
        log.info("🔍 开始获取当前用户ID...");

        // 步骤1: 首先尝试从Session中获取用户ID（登录时设置的）
        Long userIdFromSession = (Long) request.getSession().getAttribute("userId");
        if (userIdFromSession != null) {
            log.info("✅ 从Session获取用户ID成功: {}", userIdFromSession);
            return userIdFromSession;
        } else {
            log.warn("❌ Session中未找到userId，检查Session信息:");
            log.warn("   - Session ID: {}", request.getSession().getId());
            log.warn("   - Session中存在的属性: {}", request.getSession().getAttributeNames().toString());

            // 检查Session中是否有currentUser对象
            Object currentUser = request.getSession().getAttribute("currentUser");
            if (currentUser != null) {
                log.warn("   - currentUser对象: {}", currentUser.getClass().getName());
                if (currentUser instanceof User) {
                    User sessionUser = (User) currentUser;
                    log.warn("   - currentUser用户ID: {}", sessionUser.getId());
                    return sessionUser.getId();
                }
            }
        }

        // 步骤2: 如果Session中没有，尝试从Spring Security获取
        log.info("🔄 尝试从Spring Security获取用户信息...");
        log.info("   - Authentication对象: {}", authentication);
        log.info("   - Authentication是否为null: {}", authentication == null);

        if (authentication != null) {
            log.info("   - Principal: {}", authentication.getPrincipal());
            log.info("   - Principal是否为null: {}", authentication.getPrincipal() == null);
            log.info("   - Principal类型: {}", authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : "null");

            Object principal = authentication.getPrincipal();
            if (principal != null) {
                if (principal instanceof User) {
                    Long userId = ((User) principal).getId();
                    log.info("✅ 从Spring Security获取用户ID成功: {}", userId);
                    return userId;
                } else if (principal instanceof String) {
                    log.warn("⚠️ Principal是字符串类型: {}", principal);
                    // 尝试通过用户名查找用户
                    try {
                        Optional<User> userOpt = userService.findByUsername((String) principal);
                        if (userOpt.isPresent()) {
                            Long userId = userOpt.get().getId();
                            log.info("✅ 通过用户名查找获取用户ID成功: {}", userId);
                            return userId;
                        } else {
                            log.warn("❌ 通过用户名未找到用户: {}", principal);
                        }
                    } catch (Exception e) {
                        log.error("❌ 通过用户名查找用户时出错: {}", e.getMessage(), e);
                    }
                }
            }
        }

        // 步骤3: 都没有的话，使用测试用户
        log.warn("🚨 所有认证方式都失败，使用测试用户逻辑");
        return getOrCreateTestUser();
    }

    /**
     * 获取或创建测试用户（用于未登录情况下的测试）
     */
    private Long getOrCreateTestUser() {
        try {
            // 尝试查找测试用户
            Optional<User> testUserOpt = userService.findByUsername("testuser");
            if (testUserOpt.isPresent()) {
                Long userId = testUserOpt.get().getId();
                log.info("找到测试用户，ID: {}", userId);
                return userId;
            }

            // 创建测试用户
            log.info("未找到测试用户，创建新的测试用户...");
            User testUser = userService.registerByEmail("testuser", "test@example.com", "test123");
            log.info("创建测试用户成功，ID: {}", testUser.getId());
            return testUser.getId();

        } catch (Exception e) {
            log.error("创建测试用户失败: {}", e.getMessage(), e);
            throw new IllegalStateException("无法创建测试用户: " + e.getMessage());
        }
    }
    
    /**
     * 上传图片 - 支持条件式AI分析
     */
    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false, defaultValue = "general") String category,
            @RequestParam(value = "is_analysis_requested", required = false, defaultValue = "false") Boolean isAnalysisRequested,
            Authentication authentication,
            HttpServletRequest request) {

        log.info("=== 图片上传API调用（支持条件式AI分析） ===");
        log.info("文件名: {}", file != null ? file.getOriginalFilename() : "null");
        log.info("文件大小: {} bytes", file != null ? file.getSize() : "null");
        log.info("描述: {}", description);
        log.info("分类: {}", category);
        log.info("🤖 AI分析请求: {}", isAnalysisRequested ? "是" : "否");

        // 记录关键数据流状态
        if (isAnalysisRequested) {
            log.info("🔥 将启动AI分析工作流：Multipart-to-Base64转换 -> 并行调用模型 -> 数据持久化");
        } else {
            log.info("📁 仅执行文件上传和基础存储，跳过AI分析");
        }

        Map<String, Object> response = new HashMap<>();

        try {
            if (file == null || file.isEmpty()) {
                log.warn("❌ 上传文件为空");
                response.put("success", false);
                response.put("message", "请选择要上传的文件");
                return ResponseEntity.badRequest().body(response);
            }

            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            log.info("当前用户ID: {}", userId);

            // 上传文件到服务器
            log.info("开始上传文件到服务器...");
            String fileUrl = fileUploadUtils.uploadImage(file);
            log.info("文件上传成功，URL: {}", fileUrl);

            // 上传图片并保存到数据库
            log.info("开始保存文件信息到数据库...");
            UserFile userFile = userFileService.saveImageFile(userId, file, fileUrl);
            log.info("文件信息保存成功，数据库ID: {}", userFile.getId());

            // 条件式AI分析逻辑
            Map<String, Object> analysisData = null;
            if (isAnalysisRequested) {
                log.info("🔥 ===== 开始条件式AI分析 =====");
                analysisData = performConditionalAnalysis(userFile, file, "image", authentication, request);
                log.info("✅ ===== 条件式AI分析完成 =====");
            } else {
                log.info("⏭️ 跳过AI分析，仅返回文件上传结果");
            }

            // 构造响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", userFile.getId());
            responseData.put("url", userFile.getFileUrl());
            responseData.put("filename", userFile.getOriginalFilename());
            responseData.put("size", userFile.getFormattedFileSize());
            responseData.put("type", "image");
            responseData.put("uploadTime", userFile.getUploadTime());
            responseData.put("description", description != null ? description : "");
            responseData.put("category", category);
            responseData.put("downloadCount", userFile.getDownloadCount());

            // 如果进行了AI分析，添加分析结果
            if (analysisData != null) {
                responseData.put("ai_analysis", analysisData);
                log.info("📊 AI分析结果已添加到响应数据中");
            }

            response.put("success", true);
            response.put("message", isAnalysisRequested ? "图片上传并AI分析完成" : "图片上传成功");
            response.put("data", responseData);

            log.info("✅ 图片上传{}完成，文件ID: {}, URL: {}",
                isAnalysisRequested ? "及AI分析" : "", userFile.getId(), userFile.getFileUrl());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("❌ 图片上传参数错误：{}", e.getMessage());
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
     * 上传视频 - 支持条件式AI分析
     */
    @PostMapping("/upload/video")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "is_analysis_requested", required = false, defaultValue = "false") Boolean isAnalysisRequested,
            Authentication authentication,
            HttpServletRequest request) {

        log.info("=== 视频上传API调用（支持条件式AI分析） ===");
        log.info("文件名: {}", file != null ? file.getOriginalFilename() : "null");
        log.info("文件大小: {} bytes", file != null ? file.getSize() : "null");
        log.info("描述: {}", description);
        log.info("🤖 AI分析请求: {}", isAnalysisRequested ? "是" : "否");

        // 记录关键数据流状态
        if (isAnalysisRequested) {
            log.info("🔥 将启动AI分析工作流：Multipart-to-Base64转换 -> 并行调用模型 -> 数据持久化");
        } else {
            log.info("📁 仅执行文件上传和基础存储，跳过AI分析");
        }

        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            log.info("当前用户ID: {}", userId);

            // 上传文件到服务器
            log.info("开始上传视频文件到服务器...");
            String fileUrl = fileUploadUtils.uploadVideo(file);
            log.info("视频文件上传成功，URL: {}", fileUrl);

            // 上传视频并保存到数据库
            log.info("开始保存视频文件信息到数据库...");
            UserFile userFile = userFileService.saveVideoFile(userId, file, fileUrl);
            log.info("视频文件信息保存成功，数据库ID: {}", userFile.getId());

            // 条件式AI分析逻辑
            Map<String, Object> analysisData = null;
            if (isAnalysisRequested) {
                log.info("🔥 ===== 开始条件式AI分析 =====");
                analysisData = performConditionalAnalysis(userFile, file, "video", authentication, request);
                log.info("✅ ===== 条件式AI分析完成 =====");
            } else {
                log.info("⏭️ 跳过AI分析，仅返回文件上传结果");
            }

            // 构造响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", userFile.getId());
            responseData.put("url", userFile.getFileUrl());
            responseData.put("filename", userFile.getOriginalFilename());
            responseData.put("size", userFile.getFormattedFileSize());
            responseData.put("type", "video");
            responseData.put("uploadTime", userFile.getUploadTime());
            responseData.put("description", description != null ? description : "");
            responseData.put("downloadCount", userFile.getDownloadCount());

            // 如果进行了AI分析，添加分析结果
            if (analysisData != null) {
                responseData.put("ai_analysis", analysisData);
                log.info("📊 AI分析结果已添加到响应数据中");
            }

            response.put("success", true);
            response.put("message", isAnalysisRequested ? "视频上传并AI分析完成" : "视频上传成功");
            response.put("data", responseData);

            log.info("✅ 视频上传{}完成，文件ID: {}, URL: {}",
                isAnalysisRequested ? "及AI分析" : "", userFile.getId(), userFile.getFileUrl());
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
            Authentication authentication,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            
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
            Authentication authentication,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            
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
            Authentication authentication,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            
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
                    // 上传文件到服务器
                    String fileUrl = fileUploadUtils.uploadImage(file);
                    UserFile userFile = userFileService.saveImageFile(userId, file, fileUrl);
                    
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
            Authentication authentication,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            log.info("👤 [用户认证] 获取用户{}的文件和AI分析结果列表", userId);

            // 使用新的合并查询方法，同时获取原始文件和AI分析结果
            Map<String, Object> resultData = userFileService.getUserFilesWithAnalysisPaged(
                userId, page, size, type, filename);

            // 添加调试日志：打印完整的返回JSON数据
            try {
                String responseDataJson = objectMapper.writeValueAsString(resultData);
                log.info("📋 [API返回] /api/file/list 完整响应数据:");
                log.info("📋 [API返回] 数据长度: {} 字符", responseDataJson.length());
                log.info("📋 [API返回] 内容预览: {}", responseDataJson.length() > 500 ? responseDataJson.substring(0, 500) + "..." : responseDataJson);

                // 统计AI分析结果数量
                List<?> filesList = (List<?>) resultData.get("files");
                long analysisCount = filesList.stream()
                        .filter(file -> {
                            try {
                                Object hasAnalysis = file.getClass().getMethod("getHasAnalysis").invoke(file);
                                return Boolean.TRUE.equals(hasAnalysis);
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .count();
                log.info("📊 [API返回] 文件总数: {}, 包含AI分析结果的数量: {}", filesList.size(), analysisCount);
            } catch (Exception e) {
                log.warn("⚠️ [API返回] 序列化响应数据时出错: {}", e.getMessage());
            }

            response.put("success", true);
            response.put("data", resultData);
            response.put("message", "获取成功");

            log.info("✅ [API返回] /api/file/list 接口处理完成");
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
            Authentication authentication,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            
            // 获取文件详情
            UserFile userFile = userFileService.getUserFile(userId, fileId).orElse(null);
            
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
            Authentication authentication,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            
            // 增加下载次数并获取文件信息
            boolean success = userFileService.incrementDownloadCount(fileId);
            if (!success) {
                response.put("success", false);
                response.put("message", "文件不存在或无权限下载");
                return ResponseEntity.notFound().build();
            }

            UserFile userFile = userFileService.getUserFile(userId, fileId).orElse(null);
            
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
            Authentication authentication,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            
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
            Authentication authentication,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            
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
            Authentication authentication,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            
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
            Authentication authentication,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId(authentication, request);
            
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

    /**
     * AI媒体分析接口 - 同时调用外部模型和Python微服务
     * 支持图片和视频，使用Multipart接收文件并存储
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeMedia(
            @RequestParam("file") MultipartFile mediaFile,
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication,
            HttpServletRequest request) {

        log.info("🎯 ===== 开始双重AI分析请求 =====");

        // 如果未提供提示词，使用写死的专业柔道分析提���词
        if (prompt == null || prompt.trim().isEmpty()) {
            prompt = getExternalModelPrompt();
            log.info("📝 使用默认专业柔道分析提示词: {} 字符", prompt.length());
        } else {
            log.info("📝 使用自定义提示词: {}", prompt);
        }
        log.info("📁 文件名: {}", mediaFile.getOriginalFilename());
        log.info("📊 文件大小: {} bytes", mediaFile.getSize());
        log.info("📄 MIME类型: {}", mediaFile.getContentType());
        log.info("📝 描述: {}", description != null ? description : "无");

        Map<String, Object> response = new HashMap<>();

        // 声明变量在try块外部，确保在catch块中可以访问
        String mediaType = "";
        String mediaBase64 = "";

        try {
            // 文件验证
            if (mediaFile == null || mediaFile.isEmpty()) {
                log.error("❌ 上传文件为空");
                response.put("success", false);
                response.put("message", "请选择要上传的文件");
                return ResponseEntity.badRequest().body(response);
            }

            // 使用动态用户ID获取逻辑，确保分析结果正确关联到当前用户
            Long userId = getCurrentUserId(authentication, request);
            log.info("🔍 [用户认证] 认证机制: Session + Spring Security 双重认证");
            log.info("👤 [用户认证] 获取到的用户ID: {}", userId);
            log.info("💾 [数据存储] 将使用用户ID {} 保存到ai_analysis表", userId);

            // 检测媒体类型
            String contentType = mediaFile.getContentType();
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    mediaType = "image";
                } else if (contentType.startsWith("video/")) {
                    mediaType = "video";
                } else {
                    // 通过文件扩展名检测
                    String filename = mediaFile.getOriginalFilename().toLowerCase();
                    if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png") || filename.endsWith(".gif")) {
                        mediaType = "image";
                    } else if (filename.endsWith(".mp4") || filename.endsWith(".avi") || filename.endsWith(".mov") || filename.endsWith(".mkv")) {
                        mediaType = "video";
                    } else {
                        throw new IllegalArgumentException("不支持的文件类型: " + contentType + "，文件名: " + filename);
                    }
                }
            } else {
                throw new IllegalArgumentException("无法确定文件类型");
            }

            log.info("🎬 检测到媒体���型: {}", mediaType);

            // 1. 存储文件到本地
            log.info("💾 步骤1: 开始存储文件到本地...");
            String fileUrl;
            if ("image".equals(mediaType)) {
                fileUrl = fileUploadUtils.uploadImage(mediaFile);
            } else {
                fileUrl = fileUploadUtils.uploadVideo(mediaFile);
            }
            log.info("✅ 文件存储成功 - URL: {}", fileUrl);

            // 2. 保存文件信息到数据库
            log.info("🗄️  步骤2: 开始保存文件信息到数据库...");
            UserFile userFile;
            if ("image".equals(mediaType)) {
                userFile = userFileService.saveImageFile(userId, mediaFile, fileUrl);
            } else {
                userFile = userFileService.saveVideoFile(userId, mediaFile, fileUrl);
            }
            log.info("✅ 文件信息保存成功 - 数据库ID: {}", userFile.getId());

            // 3. 更新文件描述
            if (description != null && !description.trim().isEmpty()) {
                log.info("📝 步骤3: 更新文件描述...");
                userFileService.updateFileDescription(userId, userFile.getId(), description);
            }

            // 4. 创建AI分析记录（跳过Base64转换，直接使用二进制流传输）
            log.info("📝 步骤4: 创建AI分析记录（二进制流传输模式）...");
            log.info("🔗 [数据关联] userFileId: {} -> userId: {}", userFile.getId(), userId);
            log.info("📋 [数据表] ai_analysis表记录创建中...");
            AIAnalysis aiAnalysis = aiAnalysisService.createAnalysis(
                userFile.getId(), userId, mediaType, prompt);
            log.info("✅ [数据存储] AI分析记录创建成功 - 分析ID: {}, 关联用户ID: {}",
                aiAnalysis.getId(), aiAnalysis.getUserId());

            // 初始化结果变量
            String externalModelResult = null;
            String pythonServiceResult = null;
            Map<String, Object> pythonServiceData = null;
            String annotatedMediaData = null;
            String annotatedMediaUrl = null;

            log.info("🔄 ===== 开始AI分析工作流 =====");
            log.info("📋 工作流概述: 接收文件 → 调用模型 → 下载标注文件 → 保存到后端 → 更新数据库");
            log.info("📊 文件信息: 类型={}, 大小={}bytes", mediaType, mediaFile.getSize());

            // 1. 调用外部模型（火山引擎）获取文字说明
            log.info("🔥 步骤1: 开始调用外部模型获取{}文字说明...", mediaType);
            try {
                externalModelResult = callExternalModel(mediaFile, prompt, mediaType);
                log.info("✅ 外部模型调用成功 - 响应长度: {} 字符", externalModelResult.length());
            } catch (Exception e) {
                log.error("❌ 外部模型调用失败: {}", e.getMessage());
                throw e;
            }

            // 2. 调用Python微服务获取标点媒体数据（使用二进制流传输）
            log.info("🐍 步骤2: 开始调用Python微服务获取标点{}（二进制流传输）...", mediaType);
            try {
                pythonServiceResult = callPythonMicroserviceBinary(mediaFile, mediaType);
                log.info("✅ Python微服务二进制流调用成功 - 响应长度: {} 字符", pythonServiceResult.length());

                // 解析Python微服务响应
                pythonServiceData = objectMapper.readValue(pythonServiceResult, Map.class);
                Integer code = (Integer) pythonServiceData.get("code");
                String resultType = (String) pythonServiceData.get("result_type");

                // 根据媒体类型提取标注文件URL
                String annotatedFileUrl = (String) pythonServiceData.get("annotated_file_url");
                String annotatedFilename = (String) pythonServiceData.get("annotated_filename");

                if (annotatedFileUrl != null && !annotatedFileUrl.isEmpty()) {
                    log.info("✅ Python微服务返回标注文件URL: {}", annotatedFileUrl);
                    log.info("✅ 标注文件名: {}", annotatedFilename);

                    // 处理微服务返回的URL，替换localhost为服务器地��
                    String processedFileUrl = replaceLocalhostWithServerAddress(annotatedFileUrl);
                    if (!processedFileUrl.equals(annotatedFileUrl)) {
                        log.info("🔄 [URL处理] 已替换URL地址:");
                        log.info("📡 [URL处理] 原始URL: {}", annotatedFileUrl);
                        log.info("📡 [URL处理] 处理后URL: {}", processedFileUrl);
                    }

                    // 从URL下载文件并保存到后端
                    try {
                        log.info("🔄 开始从URL下载标注文件...");
                        log.info("📡 下载URL: {}", processedFileUrl);
                        byte[] fileBytes = downloadFileFromUrl(processedFileUrl, mediaType);

                        if (fileBytes != null && fileBytes.length > 0) {
                            // 使用FileUploadUtils保存到正确的后端目录
                            FileUploadUtils.AnnotatedFileResult result = fileUploadUtils.saveAnnotatedFileFromBytes(
                                fileBytes, mediaType, annotatedFilename
                            );

                            annotatedMediaUrl = result.getFileUrl();
                            log.info("✅ 标注文件已保存到后端: {}", annotatedMediaUrl);
                            log.info("📊 文件大小: {} bytes", fileBytes.length);
                            log.info("📁 保存的文件名: {}", result.getFilename());

                            // 数据库更新将在后续的saveAnalysisResults方法中处理
                            log.info("💾 数据库更新将在AIAnalysisService中处理");

                        } else {
                            log.error("❌ 下载的文件为空");
                            throw new RuntimeException("下载的文件为空");
                        }
                    } catch (Exception downloadException) {
                        log.error("❌ 从URL下载文件失败: {}", downloadException.getMessage(), downloadException);
                        throw new RuntimeException("从URL下载标注文件失败: " + downloadException.getMessage());
                    }

                    annotatedMediaData = null; // 不再需要Base64数据
                } else {
                    log.warn("⚠️ Python微服务未返回标注文件URL");
                    // 兼容旧格式Base64返回
                    if ("image".equals(mediaType)) {
                        annotatedMediaData = (String) pythonServiceData.get("image_base64_data");
                    } else if ("video".equals(mediaType)) {
                        annotatedMediaData = (String) pythonServiceData.get("video_base64_data");
                    }
                }

                log.info("📊 Python响应解析 - code: {}, result_type: {}, 标注URL: {}",
                    code, resultType, annotatedMediaUrl);
                log.info("📊 标注数据获取方式: {}",
                    annotatedMediaUrl != null ? "文件URL引用" : "Base64数据(兼容)");
            } catch (Exception e) {
                log.error("❌ Python微服务调用失败: {}", e.getMessage());
                throw e;
            }

            // 3. 保存AI分析结果到数据库
            log.info("💾 步骤3: 开始保存AI分析结果到数据库...");
            try {
                aiAnalysis = aiAnalysisService.saveAnalysisResults(
                    aiAnalysis.getId(),
                    externalModelResult,
                    pythonServiceResult,
                    annotatedMediaUrl   // 传递已保存的文件URL
                );
                log.info("✅ AI分析结果保存成功 - 分析ID: {}", aiAnalysis.getId());
                log.info("📊 数据库记录更新完成:");
                log.info("   - 分析状态: {}", aiAnalysis.getAnalysisStatus());
                log.info("   - 外部模型结果长度: {} 字符",
                    aiAnalysis.getExternalModelResult() != null ? aiAnalysis.getExternalModelResult().length() : 0);
                log.info("   - 标注文件URL: {}", aiAnalysis.getAnnotatedMediaUrl());
                log.info("   - 标注文件名: {}", aiAnalysis.getAnnotatedFilename());

                // 确保使用正确的标注文件URL
                if (annotatedMediaUrl == null) {
                    annotatedMediaUrl = aiAnalysis.getAnnotatedMediaUrl();
                    log.info("🔧 从数据库记录更新标注文件URL: {}", annotatedMediaUrl);
                }
                log.info("🎯 最终标注文件URL: {}", annotatedMediaUrl);

                // 工作流完成总结
                log.info("🎉 ===== AI分析工作流完成 =====");
                log.info("✅ 工作流状态: 成功完成");
                log.info("📁 标注文件已保存到后端目录: {}", annotatedMediaUrl);
                log.info("💾 数据库记录已更新: 分析ID={}", aiAnalysis.getId());
                log.info("🔗 前端可访问URL: {}", annotatedMediaUrl);

                // 更新响应结果，添加分析结果信息
                if (annotatedMediaUrl != null) {
                    response.put("annotated_media_url", annotatedMediaUrl);
                    response.put("annotated_filename", aiAnalysis.getAnnotatedFilename());
                }

                // 添加文字描述文件信息
                if (aiAnalysis.getExternalModelResultUrl() != null) {
                    response.put("text_description_url", aiAnalysis.getExternalModelResultUrl());
                    response.put("text_description_filename", aiAnalysis.getExternalModelResultFilename());
                    log.info("📝 文字描述文件URL已添加到响应: {}", aiAnalysis.getExternalModelResultUrl());
                }

                // 添加数据库中的分析结果信息
                response.put("analysis_id", aiAnalysis.getId());
                response.put("analysis_status", aiAnalysis.getAnalysisStatus());
                response.put("analysis_time", aiAnalysis.getAnalysisTime());
                response.put("saved_external_model_result", aiAnalysis.getExternalModelResult());
                response.put("has_description",
                    (aiAnalysis.getExternalModelResultUrl() != null && !aiAnalysis.getExternalModelResultUrl().trim().isEmpty()) ||
                    (aiAnalysis.getExternalModelResult() != null && !aiAnalysis.getExternalModelResult().trim().isEmpty())
                );

                log.info("📊 数据库保存结果检查:");
                log.info("   - 外部模型结果已保存: {}", aiAnalysis.getExternalModelResult() != null ? "✅" : "❌");
                log.info("   - 标注文件已保存: {}", annotatedMediaUrl != null ? "✅" : "❌");
                log.info("   - 分析ID: {}", aiAnalysis.getId());

            } catch (Exception saveEx) {
                log.error("❌ 保存AI分析结果失败: {}", saveEx.getMessage());
                // 保存失败不影响返回结果，但记录错误
                response.put("analysis_save_error", saveEx.getMessage());
            }

            // 4. 构造标准化响应结果（模仿/api/file/list模式）
            log.info("🏗️  步骤4: 构造标准化响应结果，消除Base64传输...");
            log.debug("📊 数据流分析 - 原始数据统计:");
            log.debug("   - externalModelResult长度: {} 字符", externalModelResult != null ? externalModelResult.length() : 0);
            log.debug("   - annotatedMediaData长度: {} 字符", annotatedMediaData != null ? annotatedMediaData.length() : 0);
            log.debug("   - annotatedMediaUrl: {}", annotatedMediaUrl);

            // 构建data对象，包含三个核心结果
            Map<String, Object> data = new HashMap<>();

            // 核心结果1: 源文件
            log.debug("📁 构建源文件信息...");
            Map<String, Object> sourceFile = new HashMap<>();
            sourceFile.put("file_id", userFile.getId());
            sourceFile.put("file_url", userFile.getFileUrl());
            sourceFile.put("media_type", mediaType);
            sourceFile.put("original_filename", userFile.getOriginalFilename());
            data.put("source_file", sourceFile);
            log.info("✅ 源文件信息已构建: {} (ID: {})", userFile.getOriginalFilename(), userFile.getId());

            // 核心结果2: 标记文件
            log.debug("🎯 构建标记文件信息...");
            Map<String, Object> markedFile = new HashMap<>();
            if (annotatedMediaUrl != null) {
                markedFile.put("file_url", annotatedMediaUrl);
                markedFile.put("media_type", mediaType);
                markedFile.put("annotation_status", "completed");
                log.info("✅ 标记文件URL已准备: {}", annotatedMediaUrl);
                log.debug("   - Base64数据已转换为文件引用，传输优化: -33%");
            } else {
                markedFile.put("annotation_status", "failed");
                markedFile.put("error", "标注文件生成失败");
                log.warn("⚠️ 标记文件生成失败");
            }
            data.put("marked_file", markedFile);

            // 核心结果3: 文本描述
            log.debug("📝 构建文本描述信息...");
            Map<String, Object> textDescription = new HashMap<>();
            if (externalModelResult != null && !externalModelResult.trim().isEmpty()) {
                String cleanedDescription = extractTextDescription(externalModelResult);
                textDescription.put("description", cleanedDescription);
                textDescription.put("status", "completed");
                log.info("✅ 文本描述已提取: {} 字符", cleanedDescription.length());
                log.debug("   - 原始数据已清理，移除JSON格式化标记");
            } else {
                textDescription.put("description", "");
                textDescription.put("status", "empty");
                log.warn("⚠️ 文本描述为空");
            }
            data.put("text_description", textDescription);

            // 构造标准响应格式
            log.debug("🏗️ 构造最终响应结构...");
            response.put("success", true);
            response.put("message", "AI分析完成");
            response.put("data", data);

            log.info("📈 新格式响应构造完成 - 完全消除Base64传输:");
            log.info("   - 源文件: {} (ID: {})", userFile.getOriginalFilename(), userFile.getId());
            log.info("   - 标记文件: {} (文件引用)", annotatedMediaUrl != null ? "✅" : "❌");
            log.info("   - 文本描述: {} (纯文本)", externalModelResult != null ? "✅" : "❌");
            log.info("   - 数据传输优化: Base64格式已完全移除");
            log.info("   - 响应格式标准化: 采用了/api/file/list相同的success/data模式");

            // 记录完成状态和关键信息
            log.info("   - 数据库文件ID: {}", userFile.getId());
            log.info("   - AI分析ID: {}", aiAnalysis.getId());
            log.info("   - 分析状态: {}", aiAnalysis.getAnalysisStatus());
            log.info("✅ ===== AI{}分析请求完成（新响应格式） =====", mediaType);

        } catch (Exception e) {
            log.error("💥 ===== 双重AI{}分析请求失败 =====", mediaType);
            log.error("❌ 异常类型: {}", e.getClass().getSimpleName());
            log.error("❌ 异常消息: {}", e.getMessage());

            // 打印异常堆栈的关键信息
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (int i = 0; i < Math.min(3, stackTrace.length); i++) {
                log.error("❌ 堆栈[{}]: {}.{}():{}",
                    i,
                    stackTrace[i].getClassName(),
                    stackTrace[i].getMethodName(),
                    stackTrace[i].getLineNumber());
            }

            response.put("success", false);
            response.put("message", "双重分析失败: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            response.put("media_type", mediaType);
        }

        return ResponseEntity.ok(response);
    }

    
    /**
     * 将MultipartFile转换为Base64字符串
     */
    private String convertMultipartFileToBase64(MultipartFile mediaFile, String mediaType) throws IOException {
        log.info("🔄 开始将{}转换为Base64（类型: {}）", mediaFile.getOriginalFilename(), mediaType);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytes = 0;

            try (InputStream inputStream = mediaFile.getInputStream()) {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
            }

            String base64Data = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            log.info("✅ {} Base64转换完成 - 原始大小: {} bytes, Base64长度: {} 字符",
                mediaFile.getOriginalFilename(), totalBytes, base64Data.length());

            return base64Data;
        }
    }

    /**
     * 调用外部模型（火山引擎）获取文字说明 - 支持图片和视频
     */
    private String callExternalModel(MultipartFile mediaFile, String prompt, String mediaType) throws Exception {
        log.info("📦 使用Base64传输到火山引擎API（兼容模式）");
        return callExternalModelBase64(mediaFile, prompt, mediaType);
    }

    /**
     * 使用Base64传输文件到火山引擎API（火山引擎要求Base64格式）
     */
    private String callExternalModelBase64(MultipartFile mediaFile, String prompt, String mediaType) throws Exception {
        log.info("🏗️  构造火山引擎Base64格式请求体...");

        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "ep-20251204141721-tgjzh");

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");

        List<Map<String, Object>> content = new ArrayList<>();

        // 文本内容
        log.info("📝 添加文本内容...");
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);
        content.add(textContent);

        // 媒体内容（图片或视频）- 火山引擎API要求Base64格式
        Map<String, Object> mediaContent = new HashMap<>();
        String mediaBase64ForExternal = "";

        try {
            mediaBase64ForExternal = convertMultipartFileToBase64(mediaFile, mediaType);
            log.info("🔄 生成{}Base64数据，长度: {} 字符", mediaType, mediaBase64ForExternal.length());

            if ("image".equals(mediaType)) {
                log.info("🖼️ 添加图片内容...");
                mediaContent.put("type", "image_url");
                Map<String, String> imageUrl = new HashMap<>();
                String fullImageUrl = "data:image/jpeg;base64," + mediaBase64ForExternal;
                imageUrl.put("url", fullImageUrl);
                mediaContent.put("image_url", imageUrl);
            } else if ("video".equals(mediaType)) {
                log.info("🎬 添加视频内容...");
                mediaContent.put("type", "video_url");
                Map<String, String> videoUrl = new HashMap<>();
                String fullVideoUrl = "data:video/mp4;base64," + mediaBase64ForExternal;
                videoUrl.put("url", fullVideoUrl);
                mediaContent.put("video_url", videoUrl);
            }
        } catch (Exception e) {
            log.error("❌ 转换Base64失败: {}", e.getMessage());
            throw new RuntimeException("外部模型Base64转换失败: " + e.getMessage());
        }
        content.add(mediaContent);

        message.put("content", content);
        messages.add(message);
        requestBody.put("messages", messages);

        log.info("✅ 请求体构造完成 - 包含 {} 个消息对象", messages.size());

        // 发送HTTP请求
        String result = sendHttpRequest(requestBody);

        log.info("📨 收到火山引擎{}响应 - 响应长度: {} 字符", mediaType, result.length());

        return result;
    }

    
    /**
     * 调用Python微服务获取标点媒体数据（二进制流传输优化版）
     */
    private String callPythonMicroserviceBinary(MultipartFile mediaFile, String mediaType) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("🐍 构造Python微服务二进制流请求体...");
        log.info("📊 文件信息: 名称={}, 大小={}bytes, 类型={}",
                mediaFile.getOriginalFilename(), mediaFile.getSize(), mediaFile.getContentType());

        // 文件大小验证
        if (mediaFile.getSize() > 100 * 1024 * 1024) { // 100MB限制
            throw new IllegalArgumentException("文件大小超过限制: 100MB");
        }

        // 创建multipart请求体
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("type", mediaType);

        // 添加文件部分，使用正确的MIME类型
        String contentType = mediaFile.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            // 根据媒体类型设置默认Content-Type
            contentType = "image".equals(mediaType) ? "image/jpeg" : "video/mp4";
        }

        log.info("📋 使用Content-Type: {}", contentType);

        RequestBody fileBody = RequestBody.create(
            MediaType.parse(contentType),
            mediaFile.getBytes()
        );

        requestBodyBuilder.addFormDataPart("file", mediaFile.getOriginalFilename(), fileBody);

        RequestBody body = requestBodyBuilder.build();

        // 构建HTTP请求
        Request request = new Request.Builder()
                .url(PYTHON_BINARY_URL)  // 使用全局常量配置
                .addHeader("Content-Type", "multipart/form-data")
                .post(body)
                .build();

        log.info("📤 发送二进制流请求到Python微服务...");
        log.info("🚀 Python微服务请求详情:");
        log.info("   URL: http://127.0.0.1:8000/analyze_binary");
        log.info("   Method: POST");
        log.info("   Content-Type: multipart/form-data");
        log.info("   文件大小: {} bytes", mediaFile.getSize());

        try (Response response = client.newCall(request).execute()) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            double throughput = (double) mediaFile.getSize() / (duration / 1000.0) / 1024 / 1024; // MB/s

            log.info("📥 收到Python微服务响应:");
            log.info("   状态码: {}", response.code());
            log.info("   响应时间: {}ms", duration);
            log.info("   吞吐量: {:.2f}MB/s", throughput);

            if (!response.isSuccessful()) {
                throw new RuntimeException("Python微服务返回错误: " + response.code() + " " + response.message());
            }

            String responseBody = response.body().string();
            log.info("📨 响应长度: {} 字符", responseBody.length());
            log.info("🔍 响应预览: {}", responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody);

            return responseBody;
        }
    }

    /**
     * 调用Python微服务获取标点图片数据（原Base64方法，保留作为兼容）
     */
    private String callPythonMicroservice(String mediaBase64, String mediaType) throws Exception {
        log.info("🐍 构造Python微服务请求体...");

        // 按照规范构造发送给Python微服务的请求体
        Map<String, Object> pythonRequest = new HashMap<>();
        pythonRequest.put("type", mediaType); // "image" 或 "video"
        pythonRequest.put("data_base64", mediaBase64);

        log.info("📤 准备发送请求到Python微服务 (http://127.0.0.1:5000/analyze)...");

        // 发送HTTP请求到Python微服务
        String pythonResult = callPythonService(pythonRequest);

        log.info("📨 收到Python微服务响应 - 响应长度: {} 字符", pythonResult.length());
        log.info("🔍 响应预览: {}", pythonResult.length() > 200 ? pythonResult.substring(0, 200) + "..." : pythonResult);

        return pythonResult;
    }

    /**
     * 发送HTTP请求到Python微服务
     */
    private String callPythonService(Map<String, Object> requestBody) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // 打印请求体详情
            String requestBodyStr = objectMapper.writeValueAsString(requestBody);
            log.info("🚀 Python微服务请求详情:");
            log.info("   URL: http://127.0.0.1:5000/analyze");
            log.info("   Method: POST");
            log.info("   Content-Type: application/json");
            log.info("   Body大小: {} 字符", requestBodyStr.length());
            log.info("   Body内容: {}", requestBodyStr);

            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, requestBodyStr);

            Request request = new Request.Builder()
                .url(PYTHON_BASE64_URL)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

            log.info("📤 发送请求到Python微服务...");

            try (Response response = client.newCall(request).execute()) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                log.info("📥 收到Python微服务响应:");
                log.info("   状态码: {}", response.code());
                log.info("   响应时间: {}ms", duration);
                log.info("   响应消息: {}", response.message());
                log.info("   响应头: {}", response.headers());

                if (!response.isSuccessful()) {
                    log.error("❌ Python微服务请求失败 - 状态码: {}", response.code());
                    throw new RuntimeException("Python微服务返回错误: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                log.info("📄 响应体大小: {} 字符", responseBody.length());
                log.info("📄 响应体内容: {}", responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);

                return responseBody;
            }
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.error("💥 Python微服务请求异常 - 耗时: {}ms", duration, e);
            log.error("❌ 异常类型: {}", e.getClass().getSimpleName());
            log.error("❌ 异常消息: {}", e.getMessage());

            // 打印异常堆栈的详细信息
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0) {
                log.error("❌ 异常位置: {}.{}():{}",
                    stackTrace[0].getClassName(),
                    stackTrace[0].getMethodName(),
                    stackTrace[0].getLineNumber());
            }

            throw new RuntimeException("Python微服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送HTTP请求到火山引擎
     */
    private String sendHttpRequest(Map<String, Object> requestBody) {
        long startTime = System.currentTimeMillis();

        try {
            // 打印请求体详情
            String requestBodyStr = objectMapper.writeValueAsString(requestBody);
            log.info("🚀 HTTP请求详情:");
            log.info("   URL: https://ark.cn-beijing.volces.com/api/v3/chat/completions");
            log.info("   Method: POST");
            log.info("   Headers: Authorization=Bearer [HIDDEN], Content-Type=application/json");
            log.info("   Body大小: {} 字符", requestBodyStr.length());
            log.info("   Body预览: {}", requestBodyStr.length() > 200 ? requestBodyStr.substring(0, 200) + "..." : requestBodyStr);

            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, requestBodyStr);

            Request request = new Request.Builder()
                .url("https://ark.cn-beijing.volces.com/api/v3/chat/completions")
                .addHeader("Authorization", "Bearer 80e00021-76e9-4844-b98b-2d342a17e164")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

            log.info("📤 发送HTTP请求...");

            try (Response response = client.newCall(request).execute()) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                log.info("📥 收到HTTP响应:");
                log.info("   状态码: {}", response.code());
                log.info("   响应时间: {}ms", duration);
                log.info("   响应消息: {}", response.message());
                log.info("   响应头: {}", response.headers());

                if (!response.isSuccessful()) {
                    log.error("❌ HTTP请求失败 - 状态码: {}", response.code());
                }

                String responseBody = response.body().string();
                log.info("📄 响应体大小: {} 字符", responseBody.length());
                log.info("📄 响应体内容: {}", responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);

                return responseBody;
            }
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.error("💥 HTTP请求异常 - 耗时: {}ms", duration, e);
            log.error("❌ 异常类型: {}", e.getClass().getSimpleName());
            log.error("❌ 异常消息: {}", e.getMessage());

            // 打印异常堆栈的详细信息
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0) {
                log.error("❌ 异常位置: {}.{}():{}",
                    stackTrace[0].getClassName(),
                    stackTrace[0].getMethodName(),
                    stackTrace[0].getLineNumber());
            }

            throw new RuntimeException("HTTP请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从外部模型结果中提取纯文本描述
     * 处理external_model_result字段，去除多余的JSON格式信息，只保留核心描述内容
     *
     * @param externalModelResult 原始外部模型响应数据
     * @return 清理后的纯文本描述
     */
    private String extractTextDescription(String externalModelResult) {
        log.info("🔍 开始处理external_model_result字段数据...");

        if (externalModelResult == null || externalModelResult.trim().isEmpty()) {
            log.warn("⚠️ external_model_result为空或null");
            return "";
        }

        try {
            // 尝试解析JSON格式响应（如果响应是JSON格式）
            if (externalModelResult.trim().startsWith("{") || externalModelResult.trim().startsWith("[")) {
                log.debug("📝 检测到JSON格式响应，尝试解析...");
                JsonNode jsonNode = objectMapper.readTree(externalModelResult);

                // 尝试提取常见的描述字段
                if (jsonNode.has("choices") && jsonNode.get("choices").isArray() && jsonNode.get("choices").size() > 0) {
                    // 火山引擎API格式: choices[0].message.content
                    JsonNode firstChoice = jsonNode.get("choices").get(0);
                    if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                        String content = firstChoice.get("message").get("content").asText();
                        log.info("✅ 从choices[0].message.content提取文本: {} 字符", content.length());
                        return content;
                    }
                } else if (jsonNode.has("description")) {
                    String description = jsonNode.get("description").asText();
                    log.info("✅ 从description字段提取文本: {} 字符", description.length());
                    return description;
                } else if (jsonNode.has("result")) {
                    String result = jsonNode.get("result").asText();
                    log.info("✅ 从result字段提取文本: {} 字符", result.length());
                    return result;
                } else if (jsonNode.has("content")) {
                    String content = jsonNode.get("content").asText();
                    log.info("✅ 从content字段提取文本: {} 字符", content.length());
                    return content;
                } else if (jsonNode.has("text")) {
                    String text = jsonNode.get("text").asText();
                    log.info("✅ 从text字段提取文本: {} 字符", text.length());
                    return text;
                }

                // 如果是简单的字符串值节点
                if (jsonNode.isTextual()) {
                    String textValue = jsonNode.asText();
                    log.info("✅ 提取JSON文本节点: {} 字符", textValue.length());
                    return textValue;
                }

                log.warn("⚠️ JSON格式响应中未找到标准描述字段，返回完整原始数据");
                return externalModelResult;
            }

            // 处理纯文本响应
            log.info("📄 检测到纯文本响应，直接返回");
            String cleanedText = externalModelResult.trim();

            // 移除常见的格式化标记
            cleanedText = cleanedText.replaceAll("\\*\\*(.*?)\\*\\*", "$1"); // 移除markdown粗体
            cleanedText = cleanedText.replaceAll("\\*(.*?)\\*", "$1");     // 移除markdown斜体
            cleanedText = cleanedText.replaceAll("#+\\s*", "");            // 移除markdown标题
            cleanedText = cleanedText.replaceAll("\\[\\s*\\]", "");        // 移除空括号

            log.info("✅ 文本清理完成: {} 字符", cleanedText.length());
            return cleanedText;

        } catch (Exception e) {
            log.error("❌ 解析external_model_result时发生错误: {}", e.getMessage());
            log.info("🔄 返回原始文本内容...");

            // 解析失败时返回完整原始内容
            log.warn("⚠️ JSON解析失败，返回完整原始内容，长度: {} 字符", externalModelResult.length());
            return externalModelResult;
        }
    }

    /**
     * 执行条件式AI分析
     * 调用现有的/api/file/analyze接口，避免重复实现
     *
     * @param userFile 已保存的��户文件信息
     * @param mediaFile MultipartFile格式的媒体文件
     * @param mediaType 媒体类型 (image/video)
     * @param authentication 用户认证信息
     * @param request HTTP请求对象
     * @return AI分析结果数据，包含源文件、标记文件、文字描述的元数据
     */
    private Map<String, Object> performConditionalAnalysis(UserFile userFile, MultipartFile mediaFile,
                                                          String mediaType, Authentication authentication,
                                                          HttpServletRequest request) {
        log.info("🚀 开始条件式AI分析 - 调用现有/api/file/analyze接口，媒体类型: {}", mediaType);

        Map<String, Object> analysisResult = new HashMap<>();

        try {
            // 使用任务指示中的外部模型Prompt
            String prompt = getExternalModelPrompt();
            log.info("📝 使用外部模型Prompt: {} 字符", prompt.length());

            // 直接调用现有的analyze接口，避免重复实现AI分析逻辑
            log.info("🔄 调用现有/api/file/analyze接口...");
            log.info("🔍 [方法调用] 传递认证参数: authentication={}, request={}",
                authentication != null ? authentication.getClass().getSimpleName() : "null",
                request != null ? request.getClass().getSimpleName() : "null");
            ResponseEntity<Map<String, Object>> analyzeResponse = analyzeMedia(
                mediaFile, prompt, userFile.getOriginalFilename(), authentication, request);

            if (analyzeResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseData = analyzeResponse.getBody();
                log.info("✅ /api/file/analyze接口调用成功");

                // 从响应中提取分析数据
                if (responseData != null && responseData.containsKey("data")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) responseData.get("data");

                    // 直接使用analyze接口返回的标准化数据
                    analysisResult.put("source_file", data.get("source_file"));
                    analysisResult.put("marked_file", data.get("marked_file"));
                    analysisResult.put("text_description", data.get("text_description"));
                    analysisResult.put("analysis_id", responseData.get("analysis_id"));
                    analysisResult.put("analysis_status", responseData.get("analysis_status"));
                    analysisResult.put("analysis_time", responseData.get("analysis_time"));

                    log.info("✅ AI分析数据提取完成 - 源文件: {}, 标记文件: {}, 文本描述: {}",
                        data.get("source_file") != null ? "✅" : "❌",
                        data.get("marked_file") != null ? "✅" : "❌",
                        data.get("text_description") != null ? "✅" : "❌");
                } else {
                    log.error("❌ /api/file/analyze接口返回数据格式异常");
                    analysisResult.put("error", "分析接口返回数据格式异常");
                    analysisResult.put("analysis_status", "failed");
                }
            } else {
                log.error("❌ /api/file/analyze接口调用失败，状态码: {}", analyzeResponse.getStatusCode());
                analysisResult.put("error", "分析接口调用失败，状态码: " + analyzeResponse.getStatusCode());
                analysisResult.put("analysis_status", "failed");
            }

            return analysisResult;

        } catch (Exception e) {
            log.error("💥 条件式AI分析执行失败 - 媒体类型: {}", mediaType, e);

            // 返回失败状态
            analysisResult.put("error", "AI分析失败: " + e.getMessage());
            analysisResult.put("analysis_status", "failed");
            return analysisResult;
        }
    }

    /**
     * 获取外部模型分析Prompt
     * 使用任务指示中提供的专业柔道分析Prompt
     */
    private String getExternalModelPrompt() {
        return "# 角色设定\n" +
            "你是一位国际级柔道裁判和高性能教练。请基于所提供的柔道比赛图片或视频，进行专业分析。\n\n" +
            "# 核心任务\n" +
            "观察画面中**白方（White）**和**蓝方（Blue）**两名运动员的表现，分析动作、评分并给出建议。\n\n" +
            "# 输出格式约束 (Strict JSON Output)\n" +
            "1. **绝对禁止**输出任何 Markdown 标记（如 ```json ... ```）、前言或结束语。\n" +
            "2. **必须**且**只能**返回一个标准的 JSON 对象字符串。\n" +
            "3. 所有评分必须为 **数字 (Number)** 类型（范围 0-10）。\n" +
            "4. 所有文本内容必须使用 **中文**。\n\n" +
            "# JSON 数据结构模版\n" +
            "请严格填充以下 JSON 结构，不要修改 Key（键名）：\n\n" +
            "{\n" +
            "  \"white_athlete\": {\n" +
            "    \"scores\": {\n" +
            "      \"posture\": 0,       // 动作形态评分 (数字)\n" +
            "      \"balance\": 0,       // 重心破坏评分 (数字)\n" +
            "      \"coordination\": 0,  // 投入配合评分 (数字)\n" +
            "      \"tactics\": 0        // 战术决策评分 (数字)\n" +
            "    },\n" +
            "    \"suggestions\": [      // 赛后训练建议 (数组，包含3条字符串)\n" +
            "      \"建议1...\",\n" +
            "      \"建议2...\",\n" +
            "      \"建议3...\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"blue_athlete\": {\n" +
            "    \"scores\": {\n" +
            "      \"posture\": 0,       // 动作形态评分\n" +
            "      \"balance\": 0,       // 重心破坏评分\n" +
            "      \"coordination\": 0,  // 投入配合评分\n" +
            "      \"tactics\": 0        // 战术决策评分\n" +
            "    },\n" +
            "    \"suggestions\": [\n" +
            "      \"建议1...\",\n" +
            "      \"建议2...\",\n" +
            "      \"建议3...\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"action_analysis\": \"这里填写双方运动员动作解析的详细文本...\"\n" +
            "}";
    }

    /**
     * 从URL下载文件
     * @param fileUrl 文件URL
     * @param mediaType 媒体类型
     * @return 文件字节数组
     * @throws Exception 下载异常
     */
    private byte[] downloadFileFromUrl(String fileUrl, String mediaType) throws Exception {
        log.info("🔄 开始下载{}文件: {}", mediaType, fileUrl);
        long startTime = System.currentTimeMillis();

        Request request = new Request.Builder()
            .url(fileUrl)
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("下载失败: HTTP " + response.code() + " " + response.message());
            }

            byte[] fileBytes = response.body().bytes();
            long duration = System.currentTimeMillis() - startTime;
            double fileSizeMB = fileBytes.length / 1024.0 / 1024.0;

            log.info("✅ {}文件下载完成:", mediaType);
            log.info("   文件大小: {:.2f} MB", fileSizeMB);
            log.info("   下载耗时: {} ms", duration);
            log.info("   下载速度: {:.2f} MB/s", fileSizeMB / (duration / 1000.0));

            return fileBytes;
        }
    }

    /**
     * 替换URL中的localhost为服务器地址
     * 解决微服务返回localhost地址导致的404错误
     *
     * @param originalUrl 原始URL
     * @return 处理后的URL
     */
    private String replaceLocalhostWithServerAddress(String originalUrl) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }

        // 替换各种localhost/127.0.0.1的情况，使用全局常量
        String processedUrl = originalUrl
                .replace("127.0.0.1:" + PYTHON_MICROSERVICE_PORT, SERVER_HOST + ":" + PYTHON_MICROSERVICE_PORT)
                .replace("localhost:" + PYTHON_MICROSERVICE_PORT, SERVER_HOST + ":" + PYTHON_MICROSERVICE_PORT);

        // 处理其他���口情况
        if (processedUrl.contains("127.0.0.1") || processedUrl.contains("localhost")) {
            log.warn("⚠️ [URL处理] 检测到未处理的localhost地址: {}", originalUrl);
            // 更通用的替换逻辑
            processedUrl = processedUrl.replaceAll("127\\.0\\.0\\.1:\\d+", SERVER_HOST + ":" + PYTHON_MICROSERVICE_PORT);
            processedUrl = processedUrl.replaceAll("localhost:\\d+", SERVER_HOST + ":" + PYTHON_MICROSERVICE_PORT);
        }

        log.info("🔧 [URL处理] 地址替换逻辑应用完成");
        return processedUrl;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)  // 延长写入超时
        .readTimeout(300, TimeUnit.SECONDS)    // 大幅延长读取超时，适应视频处理时间
        .build();
}