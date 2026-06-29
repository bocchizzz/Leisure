package com.campus.market.common.feign;

import com.campus.market.common.filter.RequestIdFilter;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign请求拦截器：透传Authorization/X-Request-Id + 注入X-Internal-Token
 */
@Component
@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    
    @Value("${internal.token:}")
    private String internalToken;
    
    @Override
    public void apply(RequestTemplate template) {
        // 1. 总是注入内部Token（用于服务间调用）
        if (StringUtils.hasText(internalToken)) {
            template.header(INTERNAL_TOKEN_HEADER, internalToken);
        }
        
        // 2. 尝试从当前请求上下文获取并透传Header
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 透传 Authorization
            String authorization = request.getHeader(AUTHORIZATION_HEADER);
            if (StringUtils.hasText(authorization)) {
                template.header(AUTHORIZATION_HEADER, authorization);
            }
            
            // 透传 X-Request-Id
            String requestId = request.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
            if (StringUtils.hasText(requestId)) {
                template.header(RequestIdFilter.REQUEST_ID_HEADER, requestId);
            }
        }
        
        // 打印 Feign 调用信息和透传的 Header（用于截图演示）
        log.info("Feign call: {} {} | Headers: Authorization={}, X-Request-Id={}, X-Internal-Token={}", 
                template.method(), 
                template.url(),
                template.headers().containsKey(AUTHORIZATION_HEADER) ? "[已透传]" : "[无]",
                template.headers().containsKey(RequestIdFilter.REQUEST_ID_HEADER) ? 
                    template.headers().get(RequestIdFilter.REQUEST_ID_HEADER) : "[无]",
                template.headers().containsKey(INTERNAL_TOKEN_HEADER) ? "[已注入]" : "[无]");
    }
}
