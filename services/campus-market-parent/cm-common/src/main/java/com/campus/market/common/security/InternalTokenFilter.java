package com.campus.market.common.security;

import com.campus.market.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 内部接口鉴权过滤器
 * 校验 X-Internal-Token Header，用于服务间调用和定时任务
 */
@Component
@Slf4j
public class InternalTokenFilter extends OncePerRequestFilter {
    
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Value("${internal.token:}")
    private String internalToken;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        
        // 只对 /internal/** 路径进行校验
        if (pathMatcher.match("/internal/**", path)) {
            String token = request.getHeader(INTERNAL_TOKEN_HEADER);
            
            if (!StringUtils.hasText(internalToken)) {
                log.error("Internal token not configured!");
                sendForbiddenResponse(response, "Internal token not configured");
                return;
            }
            
            if (!internalToken.equals(token)) {
                log.warn("Invalid internal token for path: {}", path);
                sendForbiddenResponse(response, "Invalid internal token");
                return;
            }
            
            log.debug("Internal token validated for path: {}", path);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Void> apiResponse = ApiResponse.error(403, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
