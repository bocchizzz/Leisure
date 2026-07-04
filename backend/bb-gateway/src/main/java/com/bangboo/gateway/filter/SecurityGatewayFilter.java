package com.bangboo.gateway.filter;

import com.bangboo.gateway.config.GatewaySecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SecurityGatewayFilter implements GlobalFilter, Ordered {
    private static final Set<String> PUBLIC_EXACT_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/tasks",
            "/api/tasks/recommendations",
            "/api/hunters/leaderboard",
            "/api/court-precedents"
    );

    private static final List<Pattern> PUBLIC_GET_PATTERNS = List.of(
            Pattern.compile("^/api/tasks/\\d+$"),
            Pattern.compile("^/api/users/\\d+$"),
            Pattern.compile("^/api/hunters/\\d+$"),
            Pattern.compile("^/api/hunters/\\d+/badges$"),
            Pattern.compile("^/uploads/.+$")
    );

    private final SecretKey jwtKey;

    public SecurityGatewayFilter(GatewaySecurityProperties properties) {
        this.jwtKey = Keys.hmacShaKeyFor(normalizeSecret(properties.getJwtSecret()));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().pathWithinApplication().value();

        if (path.startsWith("/internal/") || path.startsWith("/api/internal/")) {
            return writeError(exchange, HttpStatus.FORBIDDEN, 403, "内部接口不允许外部访问");
        }

        if (isPublicRequest(request)) {
            return chain.filter(exchange);
        }

        String token = resolveBearerToken(request);
        if (token == null) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, 401, "请先登录");
        }

        Claims claims;
        try {
            claims = parseClaims(token);
        } catch (RuntimeException ex) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, 401, "请先登录");
        }

        String status = String.valueOf(claims.get("status"));
        if ("BANNED".equals(status)) {
            return writeError(exchange, HttpStatus.FORBIDDEN, 403, "账号已被封禁");
        }

        List<String> roles = toStringList(claims.get("roles"));
        if (path.startsWith("/api/admin/") && !isAdmin(roles)) {
            return writeError(exchange, HttpStatus.FORBIDDEN, 403, "需要管理员权限");
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(headers -> appendUserHeaders(headers, claims, roles))
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicRequest(ServerHttpRequest request) {
        String path = request.getPath().pathWithinApplication().value();
        HttpMethod method = request.getMethod();
        if (PUBLIC_EXACT_PATHS.contains(path)) {
            return method == HttpMethod.GET || path.startsWith("/api/auth/");
        }
        if (method != HttpMethod.GET) {
            return false;
        }
        return PUBLIC_GET_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static void appendUserHeaders(HttpHeaders headers, Claims claims, List<String> roles) {
        headers.remove("X-User-Id");
        headers.remove("X-User-Roles");
        headers.remove("X-User-Status");
        headers.remove("X-Campus-Verified");
        headers.add("X-User-Id", String.valueOf(claims.get("uid")));
        headers.add("X-User-Roles", String.join(",", roles));
        headers.add("X-User-Status", String.valueOf(claims.get("status")));
        headers.add("X-Campus-Verified", String.valueOf(claims.get("campusVerified")));
    }

    private static String resolveBearerToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }

    private static boolean isAdmin(List<String> roles) {
        return roles.contains("ADMIN") || roles.contains("SUPER_ADMIN");
    }

    private static List<String> toStringList(Object value) {
        if (!(value instanceof Iterable<?> iterable)) {
            return List.of();
        }
        return java.util.stream.StreamSupport.stream(iterable.spliterator(), false)
                .map(String::valueOf)
                .toList();
    }

    private static byte[] normalizeSecret(String secret) {
        byte[] bytes = (secret == null ? "" : secret).getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) {
            return bytes;
        }
        return Arrays.copyOf(bytes, 32);
    }

    private static Mono<Void> writeError(
            ServerWebExchange exchange,
            HttpStatus status,
            int code,
            String message
    ) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Component
    @Order(-300)
    public static class InternalPathBlockWebFilter implements WebFilter {
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            String path = exchange.getRequest().getPath().pathWithinApplication().value();
            if (path.startsWith("/internal/") || path.startsWith("/api/internal/")) {
                return writeError(exchange, HttpStatus.FORBIDDEN, 403, "内部接口不允许外部访问");
            }
            return chain.filter(exchange);
        }
    }
}
