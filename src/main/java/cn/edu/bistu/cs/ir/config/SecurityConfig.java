package cn.edu.bistu.cs.ir.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/api/user/register", "/api/user/login", "/query/**").permitAll()
                .antMatchers("/uploads/**").permitAll()  // 允许��问上传的文件
                .antMatchers("/api/file/info").permitAll()  // 允许获取文件信息
                .antMatchers("/api/user/me").authenticated()  // 获取当前用户需要认证
                .antMatchers("/api/file/upload/**", "/api/file/delete").authenticated()  // 文件上传和删除需要认证
                .anyRequest().permitAll()
            .and()
            .formLogin()
                .disable()  // 禁用默认登录页面，使用自定义登录接口
            .logout()
                .logoutUrl("/api/user/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":true,\"message\":\"退出登录成功\"}");
                })
                .permitAll()
            .and()
            .csrf().disable()
            .sessionManagement()
                .maximumSessions(100)  // 最大会话数
                .and()
            .and()
            .sessionManagement()
                .sessionFixation().migrateSession();
        return http.build();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}