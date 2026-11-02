package cn.edu.bistu.cs.ir.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API请求日志拦截器
 * 用于调试前后端API交互问题
 */
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingInterceptor.class);
    private static final String REQUEST_START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求开始时间
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());

        // 记录请求信息
        log.info("=== API请求开始 ===");
        log.info("请求方法: {}", request.getMethod());
        log.info("请求URL: {}", request.getRequestURL());
        log.info("查询参数: {}", request.getQueryString());

        // 记录请求头中的关键信息
        log.info("Content-Type: {}", request.getContentType());
        log.info("User-Agent: {}", request.getHeader("User-Agent"));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        // 计算请求耗时
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

        log.info("=== API请求完成 ===");
        log.info("响应状态: {}", response.getStatus());
        log.info("请求耗时: {}ms", duration);

        if (ex != null) {
            log.error("请求异常: ", ex);
        }

        log.info("=================");
    }
}
