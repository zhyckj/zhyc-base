/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * JDBC 开放 API 限流策略仓储测试。
 */
class JdbcOpenApiRateLimitPolicyRepositoryTest {

  /**
   * 验证 JDBC 仓储可以按租户、应用和 API 编码读取启用限流策略。
   */
  @Test
  void shouldFindEnabledPolicyByTenantAppAndApi() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    insertPolicy(jdbcTemplate, "tenant_a", "purchase-app", "purchase.request.create",
        60, 60, "enabled");
    OpenApiRateLimitPolicyRepository repository =
        new JdbcOpenApiRateLimitPolicyRepository(jdbcTemplate);

    Optional<OpenApiRateLimitPolicy> policy = repository.findEnabledPolicy("tenant_a",
        "purchase-app", "purchase.request.create");

    assertTrue(policy.isPresent());
    assertEquals("tenant_a", policy.get().getTenantId());
    assertEquals("purchase-app", policy.get().getAppCode());
    assertEquals("purchase.request.create", policy.get().getApiCode());
    assertEquals(60, policy.get().getLimitCount());
    assertEquals(60, policy.get().getWindow().toSeconds());
  }

  /**
   * 验证禁用策略不会被运行态限流使用。
   */
  @Test
  void shouldReturnEmptyWhenPolicyDisabled() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    insertPolicy(jdbcTemplate, "tenant_a", "purchase-app", "purchase.request.create",
        60, 60, "disabled");
    OpenApiRateLimitPolicyRepository repository =
        new JdbcOpenApiRateLimitPolicyRepository(jdbcTemplate);

    Optional<OpenApiRateLimitPolicy> policy = repository.findEnabledPolicy("tenant_a",
        "purchase-app", "purchase.request.create");

    assertTrue(policy.isEmpty());
  }

  private void createTable(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
        CREATE TABLE openapi_rate_limit_policy (
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            api_code VARCHAR(128) NOT NULL,
            limit_count INT NOT NULL,
            window_seconds INT NOT NULL,
            status VARCHAR(32) NOT NULL
        )
        """);
  }

  private void insertPolicy(JdbcTemplate jdbcTemplate, String tenantId, String appCode,
      String apiCode, int limitCount, int windowSeconds, String status) {
    jdbcTemplate.update("""
        INSERT INTO openapi_rate_limit_policy (
            tenant_id, app_code, api_code, limit_count, window_seconds, status
        ) VALUES (?, ?, ?, ?, ?, ?)
        """, tenantId, appCode, apiCode, limitCount, windowSeconds, status);
  }

  private JdbcTemplate createJdbcTemplate() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:openapi_rate_limit_" + System.nanoTime()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa", "");
    dataSource.setDriverClassName("org.h2.Driver");
    return new JdbcTemplate(dataSource);
  }
}
