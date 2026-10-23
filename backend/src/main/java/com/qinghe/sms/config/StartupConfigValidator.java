package com.qinghe.sms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 启动期配置校验。
 *
 * 背景: Spring Boot 对配置属性中的占位符默认"解析不到就保留字面量",
 * 因此缺少 DB_URL 等环境变量时应用仍能启动, 直到第一次访问数据库才失败,
 * 且错误信息(Database driver claims to not accept jdbcUrl, ${DB_URL})很难定位。
 * 这里在启动阶段直接拦下, 让问题立刻可见。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupConfigValidator implements InitializingBean {

    private static final List<String> REQUIRED_ENV = List.of("DB_URL", "DB_USERNAME", "DB_PASSWORD");

    private static final String DEFAULT_JWT_SECRET = "dev-only-sms-jwt-secret-please-override-in-production";

    private final Environment environment;

    @Override
    public void afterPropertiesSet() {
        List<String> missing = REQUIRED_ENV.stream()
                .filter(key -> !StringUtils.hasText(environment.getProperty(key)))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("缺少必需的环境变量: " + String.join("、", missing)
                    + "。请在本机会话中设置后再启动(不要写入配置文件)");
        }

        String jwtSecret = environment.getProperty("JWT_SECRET");
        if (!StringUtils.hasText(jwtSecret) || DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            log.warn("JWT_SECRET 未设置或仍为默认值, 仅适合本地开发; 生产环境必须覆盖");
        }
    }
}
