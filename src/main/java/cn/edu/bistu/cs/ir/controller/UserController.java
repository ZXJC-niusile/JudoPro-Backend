package cn.edu.bistu.cs.ir.controller;

import cn.edu.bistu.cs.ir.model.User;
import cn.edu.bistu.cs.ir.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username, @RequestParam String email, @RequestParam String password) {
        User user = userService.registerByEmail(username, email, password);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (userService.getPasswordEncoder().matches(password, user.getPassword())) {
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.status(401).body("邮箱或密码错误");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(authentication.getPrincipal());
    }
}