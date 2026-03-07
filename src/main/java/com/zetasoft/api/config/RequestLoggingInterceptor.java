package com.zetasoft.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute("requestId", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        log.info("REQUEST | id={} | method={} | uri={} | query={}",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String requestId = (String) request.getAttribute("requestId");

        if (ex != null) {
            log.error("RESPONSE | id={} | method={} | uri={} | status={} | error={}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    ex.getMessage());
        } else {
            log.info("RESPONSE | id={} | method={} | uri={} | status={}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus());
        }
    }
}
