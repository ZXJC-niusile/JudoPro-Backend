package cn.edu.bistu.cs.ir.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/user/register", "/user/login", "/query/**").permitAll()
                .antMatchers("/uploads/**").permitAll()  // 允许访问上传的文件
                .antMatchers("/api/file/info").permitAll()  // 允许获取文件信息
                .antMatchers("/api/file/upload/**", "/api/file/delete").authenticated()  // 文件上传和删除需要认证
                .antMatchers("/video/upload", "/video/analyze").authenticated()
                .anyRequest().permitAll()
            .and()
            .csrf().disable();
        return http.build();
    }
}