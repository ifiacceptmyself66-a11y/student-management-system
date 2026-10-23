package com.qinghe.sms.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验 schema.sql 中初始管理员的 BCrypt 哈希与默认密码 123456 匹配,
 * 防止脚本里的哈希被误改导致登录不上。修改 schema.sql 时请同步更新此常量。
 */
class AdminPasswordHashTest {

    private static final String SEED_HASH = "$2a$10$yDKqu38sAojRlpVT9KYDQ.WV043ddNk6agKblfOR9U2U91iQDdfVC";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void seedHashShouldMatchDefaultPassword() {
        assertTrue(passwordEncoder.matches("123456", SEED_HASH));
    }

    @Test
    void seedHashShouldNotMatchOtherPassword() {
        assertFalse(passwordEncoder.matches("1234567", SEED_HASH));
    }
}
