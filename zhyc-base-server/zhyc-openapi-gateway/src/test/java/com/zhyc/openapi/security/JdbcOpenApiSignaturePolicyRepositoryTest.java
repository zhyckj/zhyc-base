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
 * JDBC 开放 API 签名策略仓储测试。
 */
class JdbcOpenApiSignaturePolicyRepositoryTest {

  /**
   * 验证 JDBC 仓储可以按租户和应用编码读取启用签名策略。
   */
  @Test
  void shouldFindEnabledPolicyByTenantAndApp() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    insertPolicy(jdbcTemplate, "tenant_a", "purchase-app", "HMAC_SHA256",
        60, 120, 1, "enabled");
    OpenApiSignaturePolicyRepository repository =
        new JdbcOpenApiSignaturePolicyRepository(jdbcTemplate);

    Optional<OpenApiSignaturePolicy> policy = repository.findEnabledPolicy("tenant_a",
        "purchase-app");

    assertTrue(policy.isPresent());
    assertEquals("tenant_a", policy.get().getTenantId());
    assertEquals("purchase-app", policy.get().getAppCode());
    assertEquals("HMAC_SHA256", policy.get().getAlgorithm());
    assertEquals(60, policy.get().getTimestampTolerance().toSeconds());
    assertEquals(120, policy.get().getNonceTtl().toSeconds());
    assertTrue(policy.get().isRequireBodyHash());
  }

  /**
   * 验证禁用签名策略不会进入运行态签名校验。
   */
  @Test
  void shouldReturnEmptyWhenPolicyDisabled() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    insertPolicy(jdbcTemplate, "tenant_a", "purchase-app", "HMAC_SHA256",
        60, 120, 1, "disabled");
    OpenApiSignaturePolicyRepository repository =
        new JdbcOpenApiSignaturePolicyRepository(jdbcTemplate);

    Optional<OpenApiSignaturePolicy> policy = repository.findEnabledPolicy("tenant_a",
        "purchase-app");

    assertTrue(policy.isEmpty());
  }

  private void createTable(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
        CREATE TABLE openapi_signature_policy (
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            algorithm VARCHAR(32) NOT NULL,
            timestamp_tolerance_seconds INT NOT NULL,
            nonce_ttl_seconds INT NOT NULL,
            require_body_hash TINYINT NOT NULL,
            status VARCHAR(32) NOT NULL
        )
        """);
  }

  private void insertPolicy(JdbcTemplate jdbcTemplate, String tenantId, String appCode,
      String algorithm, int timestampToleranceSeconds, int nonceTtlSeconds,
      int requireBodyHash, String status) {
    jdbcTemplate.update("""
        INSERT INTO openapi_signature_policy (
            tenant_id, app_code, algorithm, timestamp_tolerance_seconds,
            nonce_ttl_seconds, require_body_hash, status
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """, tenantId, appCode, algorithm, timestampToleranceSeconds, nonceTtlSeconds,
        requireBodyHash, status);
  }

  private JdbcTemplate createJdbcTemplate() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:openapi_signature_" + System.nanoTime()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa", "");
    dataSource.setDriverClassName("org.h2.Driver");
    return new JdbcTemplate(dataSource);
  }
}
