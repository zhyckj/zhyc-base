/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JDBC API Key 凭证仓储测试。
 */
class JdbcApiKeyCredentialRepositoryTest {

  /**
   * 验证 JDBC 仓储可以按访问密钥读取启用凭证，并通过 Secret 解析器获得运行态密钥。
   */
  @Test
  void shouldFindCredentialByAccessKeyAndResolveRuntimeSecret() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createOpenApiAppTable(jdbcTemplate);
    jdbcTemplate.execute("""
        CREATE TABLE openapi_api_key (
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            access_key VARCHAR(128) NOT NULL,
            secret_cipher VARCHAR(512) NOT NULL,
            status VARCHAR(32) NOT NULL,
            expire_at TIMESTAMP
        )
        """);
    insertOpenApiApp(jdbcTemplate, "tenant_a", "purchase-app", "api_key", "enabled");
    jdbcTemplate.update("""
        INSERT INTO openapi_api_key (tenant_id, app_code, access_key, secret_cipher, status, expire_at)
        VALUES (?, ?, ?, ?, ?, ?)
        """, "tenant_a", "purchase-app", "ak_test", "cipher-text", "enabled",
        "2026-12-31 00:00:00");
    AtomicReference<String> resolvedCipher = new AtomicReference<>();
    ApiSecretResolver secretResolver = secretCipher -> {
      resolvedCipher.set(secretCipher);
      return "runtime-secret";
    };
    ApiKeyCredentialRepository repository = new JdbcApiKeyCredentialRepository(jdbcTemplate, secretResolver);

    Optional<ApiKeyCredential> credential = repository.findByAccessKey("ak_test");

    assertTrue(credential.isPresent());
    assertEquals("tenant_a", credential.get().getTenantId());
    assertEquals("purchase-app", credential.get().getAppCode());
    assertEquals("ak_test", credential.get().getAccessKey());
    assertEquals("runtime-secret", credential.get().getSecretValue());
    assertEquals("enabled", credential.get().getStatus());
    assertEquals("cipher-text", resolvedCipher.get());
    assertTrue(credential.get().getExpireAt().isAfter(java.time.Instant.parse("2026-01-01T00:00:00Z")));
  }

  /**
   * 验证访问密钥不存在时返回空凭证。
   */
  @Test
  void shouldReturnEmptyWhenAccessKeyDoesNotExist() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createOpenApiAppTable(jdbcTemplate);
    jdbcTemplate.execute("""
        CREATE TABLE openapi_api_key (
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            access_key VARCHAR(128) NOT NULL,
            secret_cipher VARCHAR(512) NOT NULL,
            status VARCHAR(32) NOT NULL,
            expire_at TIMESTAMP
        )
        """);
    ApiKeyCredentialRepository repository = new JdbcApiKeyCredentialRepository(jdbcTemplate,
        secretCipher -> "runtime-secret");

    Optional<ApiKeyCredential> credential = repository.findByAccessKey("missing");

    assertTrue(credential.isEmpty());
  }

  /**
   * 验证运行态 API Key 仓储不会返回仅启用 OAuth2/OIDC 鉴权的开发者应用凭证。
   */
  @Test
  void shouldReturnEmptyWhenAppDoesNotSupportApiKeyAuthMode() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createOpenApiAppTable(jdbcTemplate);
    jdbcTemplate.execute("""
        CREATE TABLE openapi_api_key (
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            access_key VARCHAR(128) NOT NULL,
            secret_cipher VARCHAR(512) NOT NULL,
            status VARCHAR(32) NOT NULL,
            expire_at TIMESTAMP
        )
        """);
    jdbcTemplate.update("""
        INSERT INTO openapi_app (tenant_id, app_code, auth_mode, status)
        VALUES (?, ?, ?, ?)
        """, "tenant_a", "oauth-app", "oauth2", "enabled");
    jdbcTemplate.update("""
        INSERT INTO openapi_api_key (tenant_id, app_code, access_key, secret_cipher, status, expire_at)
        VALUES (?, ?, ?, ?, ?, ?)
        """, "tenant_a", "oauth-app", "ak_oauth_only", "cipher-text", "enabled",
        "2026-12-31 00:00:00");
    ApiKeyCredentialRepository repository = new JdbcApiKeyCredentialRepository(jdbcTemplate,
        secretCipher -> "runtime-secret");

    Optional<ApiKeyCredential> credential = repository.findByAccessKey("ak_oauth_only");

    assertTrue(credential.isEmpty());
  }

  private void createOpenApiAppTable(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
        CREATE TABLE openapi_app (
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            auth_mode VARCHAR(32) NOT NULL,
            status VARCHAR(32) NOT NULL
        )
        """);
  }

  private void insertOpenApiApp(JdbcTemplate jdbcTemplate, String tenantId, String appCode,
      String authMode, String status) {
    jdbcTemplate.update("""
        INSERT INTO openapi_app (tenant_id, app_code, auth_mode, status)
        VALUES (?, ?, ?, ?)
        """, tenantId, appCode, authMode, status);
  }

  private JdbcTemplate createJdbcTemplate() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:openapi_gateway_" + System.nanoTime()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa", "");
    dataSource.setDriverClassName("org.h2.Driver");
    return new JdbcTemplate(dataSource);
  }
}
