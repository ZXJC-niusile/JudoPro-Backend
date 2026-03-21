package cn.edu.bistu.cs.ir.controller;

import cn.edu.bistu.cs.ir.model.User;
import cn.edu.bistu.cs.ir.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        log.info("用户注册API调用 - username: {}, email: {}", user.getUsername(), user.getEmail());
        log.info("密码长度: {}", user.getPassword() != null ? user.getPassword().length() : 0);

        try {
            User registeredUser = userService.registerByEmail(user.getUsername(), user.getEmail(), user.getPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("user", registeredUser);

            log.info("用户注册成功 - userId: {}, username: {}", registeredUser.getId(), registeredUser.getUsername());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("注册参数错误 - username: {}, error: {}", user.getUsername(), e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("注册系统异常 - username: {}, error: {}", user.getUsername(), e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统错误: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        log.info("用户登录API调用 - username: {}", username);

        try {
            // 尝试用用户名查找
            Optional<User> userOpt = userService.findByUsername(username);

            // 如果用户名找不到，尝试用邮箱查找
            if (!userOpt.isPresent()) {
                userOpt = userService.findByEmail(username);
            }

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                log.info("找到用户 - userId: {}, username: {}", user.getId(), user.getUsername());

                if (userService.getPasswordEncoder().matches(password, user.getPassword())) {
                    log.info("密码验证成功，建立Session - userId: {}", user.getId());

                    // 创建简单的Authentication对象
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user.getUsername(), null, java.util.Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 在Session中保存用户信息
                    request.getSession().setAttribute("currentUser", user);
                    request.getSession().setAttribute("userId", user.getId());

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "登录成功");
                    response.put("user", user);

                    log.info("Session建立成功 - userId: {}, sessionId: {}", user.getId(), request.getSession().getId());
                    return ResponseEntity.ok(response);

                } else {
                    log.warn("密码验证失败 - username: {}", username);
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "用户名或密码错误");
                    return ResponseEntity.status(401).body(response);
                }
            } else {
                log.warn("用户不存在 - username: {}", username);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名或密码错误");
                return ResponseEntity.status(401).body(response);
            }

        } catch (Exception e) {
            log.error("登录系统异常 - username: {}, error: {}", username, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication, HttpServletRequest request) {
        log.info("获取当前用户API调用");

        try {
            // 首先检查Session中的用户信息
            User sessionUser = (User) request.getSession().getAttribute("currentUser");
            if (sessionUser != null) {
                log.info("从Session获取用户成功 - userId: {}", sessionUser.getId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "获取用户信息成功");
                response.put("user", sessionUser);
                return ResponseEntity.ok(response);
            }

            // 备用方案：检查Authentication
            if (authentication != null && authentication.getPrincipal() != null) {
                log.info("从Authentication获取用户成功");
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "获取用户信息成功");
                response.put("user", authentication.getPrincipal());
                return ResponseEntity.ok(response);
            }

            // 都没有找到，说明用户未登录
            log.warn("用户未登录");
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "用户未登录，请先登录"
            ));

        } catch (Exception e) {
            log.error("获取当前用户异常 - error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "系统错误: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/info/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        log.info("获取用户信息API调用 - userId: {}", userId);

        try {
            Optional<User> userOpt = userService.findById(userId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                log.info("找到用户 - userId: {}, username: {}", user.getId(), user.getUsername());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "获取用户信息成功");
                response.put("user", user);

                return ResponseEntity.ok(response);

            } else {
                log.warn("用户不存在 - userId: {}", userId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在，用户ID: " + userId);
                return ResponseEntity.status(404).body(response);
            }

        } catch (NumberFormatException e) {
            log.error("用户ID格式错误 - userId: {}, error: {}", userId, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户ID格式错误");
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("获取用户信息系统异常 - userId: {}, error: {}", userId, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        log.info("用户退出登录API调用");

        try {
            // 清除Session
            if (request.getSession(false) != null) {
                Long userId = (Long) request.getSession().getAttribute("userId");
                log.info("用户退出登录 - userId: {}", userId);
                request.getSession().invalidate();
            }

            // 清除SecurityContext
            SecurityContextHolder.clearContext();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "退出登录成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("退出登录异常 - error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "退出登录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}