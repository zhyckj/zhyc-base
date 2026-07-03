/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * JDBC 开放 API 固定窗口限流器测试。
 */
class JdbcOpenApiRateLimiterTest {

  /**
   * 验证 JDBC 限流器在共享数据库计数下拒绝超过窗口配额的请求。
   */
  @Test
  void shouldRejectRequestWhenJdbcWindowQuotaExceeded() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    Clock clock = Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC);
    OpenApiRateLimiter limiter = new JdbcOpenApiRateLimiter(jdbcTemplate, clock);
    OpenApiRateLimitPolicy policy = new OpenApiRateLimitPolicy("tenant_a", "purchase-app",
        "purchase.request.create", 2, Duration.ofMinutes(1));

    assertTrue(limiter.tryAcquire(policy, "tenant_a", "purchase-app", "purchase.request.create").isAllowed());
    assertTrue(limiter.tryAcquire(policy, "tenant_a", "purchase-app", "purchase.request.create").isAllowed());
    OpenApiRateLimitResult third = limiter.tryAcquire(policy, "tenant_a", "purchase-app",
        "purchase.request.create");

    assertEquals(false, third.isAllowed());
    assertEquals("RATE_LIMITED", third.getFailureCode());
    assertEquals(60, third.getRetryAfterSeconds());
  }

  /**
   * 验证进入新窗口后 JDBC 计数重新开始。
   */
  @Test
  void shouldResetQuotaWhenNewWindowStarts() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    OpenApiRateLimitPolicy policy = new OpenApiRateLimitPolicy("tenant_a", "purchase-app",
        "purchase.request.create", 1, Duration.ofMinutes(1));
    OpenApiRateLimiter firstWindow = new JdbcOpenApiRateLimiter(jdbcTemplate,
        Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC));
    OpenApiRateLimiter nextWindow = new JdbcOpenApiRateLimiter(jdbcTemplate,
        Clock.fixed(Instant.parse("2026-06-24T00:01:00Z"), ZoneOffset.UTC));

    assertTrue(firstWindow.tryAcquire(policy, "tenant_a", "purchase-app",
        "purchase.request.create").isAllowed());
    assertTrue(nextWindow.tryAcquire(policy, "tenant_a", "purchase-app",
        "purchase.request.create").isAllowed());
  }

  private void createTable(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
        CREATE TABLE openapi_rate_limit_counter (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            api_code VARCHAR(128) NOT NULL,
            window_seconds BIGINT NOT NULL,
            window_index BIGINT NOT NULL,
            request_count INT NOT NULL DEFAULT 0,
            expires_at TIMESTAMP NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            UNIQUE (tenant_id, app_code, api_code, window_seconds, window_index)
        )
        """);
  }

  private JdbcTemplate createJdbcTemplate() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:openapi_rate_limiter_" + System.nanoTime()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa", "");
    dataSource.setDriverClassName("org.h2.Driver");
    return new JdbcTemplate(dataSource);
  }
}
