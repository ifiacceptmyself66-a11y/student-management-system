package com.qinghe.sms.security;

import com.qinghe.sms.entity.Admin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 不依赖 Spring 容器与数据库, 只验证 JWT 的签发与解析。
 */
class JwtUtilTest {

    private static final String SECRET = "unit-test-secret-key-with-32-bytes-length";

    private LoginAdmin buildAdmin() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("irrelevant");
        admin.setNickname("超级管理员");
        admin.setStatus(1);
        return new LoginAdmin(admin);
    }

    @Test
    void shouldGenerateAndParseToken() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 120);

        String token = jwtUtil.generate(buildAdmin());
        Claims claims = jwtUtil.parse(token);

        assertEquals("admin", claims.getSubject());
        assertEquals(1, ((Number) claims.get("adminId")).intValue());
        assertEquals(7200, jwtUtil.getExpireSeconds());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void shouldRejectMalformedToken() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 120);
        assertThrows(JwtException.class, () -> jwtUtil.parse("not-a-jwt"));
    }

    @Test
    void shouldRejectTokenSignedByAnotherKey() {
        JwtUtil issuer = new JwtUtil(SECRET, 120);
        JwtUtil verifier = new JwtUtil("another-secret-key-with-32-bytes-length", 120);

        String token = issuer.generate(buildAdmin());

        assertThrows(JwtException.class, () -> verifier.parse(token));
    }
}
