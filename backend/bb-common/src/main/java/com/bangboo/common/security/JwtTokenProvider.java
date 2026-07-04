package com.bangboo.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class JwtTokenProvider {
    private final SecretKey key;
    private final Duration ttl;

    public JwtTokenProvider(String secret, Duration ttl) {
        this.key = Keys.hmacShaKeyFor(normalizeSecret(secret));
        this.ttl = ttl;
    }

    public String createToken(Long uid, List<String> roles, String status, boolean campusVerified) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(uid))
                .claim("uid", uid)
                .claim("roles", roles)
                .claim("status", status)
                .claim("campusVerified", campusVerified)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public CurrentUser parseCurrentUser(String token) {
        Claims claims = parseClaims(token);
        Long uid = toLong(claims.get("uid"));
        List<String> roles = toStringList(claims.get("roles"));
        String status = String.valueOf(claims.get("status"));
        boolean campusVerified = Boolean.TRUE.equals(claims.get("campusVerified", Boolean.class));
        return new CurrentUser(uid, roles, status, campusVerified);
    }

    private static byte[] normalizeSecret(String secret) {
        byte[] bytes = (secret == null ? "" : secret).getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) {
            return bytes;
        }
        return Arrays.copyOf(bytes, 32);
    }

    private static Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private static List<String> toStringList(Object value) {
        if (!(value instanceof Iterable<?> iterable)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        iterable.forEach(item -> result.add(String.valueOf(item)));
        return result;
    }
}
