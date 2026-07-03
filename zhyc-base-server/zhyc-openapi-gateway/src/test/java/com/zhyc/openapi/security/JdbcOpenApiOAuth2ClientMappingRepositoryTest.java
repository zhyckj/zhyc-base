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
 * JDBC OAuth2 客户端映射仓储测试。
 */
class JdbcOpenApiOAuth2ClientMappingRepositoryTest {

  /**
   * 验证 JDBC 仓储可以按租户和 OAuth2 客户端 ID 读取启用映射。
   */
  @Test
  void shouldFindEnabledMappingByTenantAndClientId() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createAppTable(jdbcTemplate);
    createTable(jdbcTemplate);
    insertApp(jdbcTemplate, "tenant_a", "purchase-app", "oauth2", "enabled");
    insertMapping(jdbcTemplate, "tenant_a", "purchase-app", "purchase-portal-client",
        "openid profile purchase.request", "enabled");
    OpenApiOAuth2ClientMappingRepository repository =
        new JdbcOpenApiOAuth2ClientMappingRepository(jdbcTemplate);

    Optional<OpenApiOAuth2ClientMapping> mapping = repository.findEnabledByTenantIdAndClientId(
        "tenant_a", "purchase-portal-client");

    assertTrue(mapping.isPresent());
    assertEquals("tenant_a", mapping.get().getTenantId());
    assertEquals("purchase-app", mapping.get().getAppCode());
    assertEquals("purchase-portal-client", mapping.get().getClientId());
    assertEquals("openid profile purchase.request", mapping.get().getAllowedScopes());
  }

  /**
   * 验证禁用映射不会被运行态 OAuth2 Token 校验使用。
   */
  @Test
  void shouldReturnEmptyWhenMappingDisabled() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createAppTable(jdbcTemplate);
    createTable(jdbcTemplate);
    insertApp(jdbcTemplate, "tenant_a", "purchase-app", "oauth2", "enabled");
    insertMapping(jdbcTemplate, "tenant_a", "purchase-app", "purchase-portal-client",
        "openid profile", "disabled");
    OpenApiOAuth2ClientMappingRepository repository =
        new JdbcOpenApiOAuth2ClientMappingRepository(jdbcTemplate);

    Optional<OpenApiOAuth2ClientMapping> mapping = repository.findEnabledByTenantIdAndClientId(
        "tenant_a", "purchase-portal-client");

    assertTrue(mapping.isEmpty());
  }

  /**
   * 验证运行态 OAuth2 映射仓储不会返回仅启用 API Key 鉴权的开发者应用客户端。
   */
  @Test
  void shouldReturnEmptyWhenAppDoesNotSupportOauth2AuthMode() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createAppTable(jdbcTemplate);
    createTable(jdbcTemplate);
    insertApp(jdbcTemplate, "tenant_a", "api-key-app", "api_key", "enabled");
    insertMapping(jdbcTemplate, "tenant_a", "api-key-app", "purchase-portal-client",
        "openid profile purchase.request", "enabled");
    OpenApiOAuth2ClientMappingRepository repository =
        new JdbcOpenApiOAuth2ClientMappingRepository(jdbcTemplate);

    Optional<OpenApiOAuth2ClientMapping> mapping = repository.findEnabledByTenantIdAndClientId(
        "tenant_a", "purchase-portal-client");

    assertTrue(mapping.isEmpty());
  }

  private void createAppTable(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
        CREATE TABLE openapi_app (
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            auth_mode VARCHAR(32) NOT NULL,
            status VARCHAR(32) NOT NULL
        )
        """);
  }

  private void createTable(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
        CREATE TABLE openapi_oauth_client (
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            client_id VARCHAR(128) NOT NULL,
            allowed_scopes VARCHAR(512) NOT NULL,
            status VARCHAR(32) NOT NULL
        )
        """);
  }

  private void insertMapping(JdbcTemplate jdbcTemplate, String tenantId, String appCode,
      String clientId, String allowedScopes, String status) {
    jdbcTemplate.update("""
        INSERT INTO openapi_oauth_client (
            tenant_id, app_code, client_id, allowed_scopes, status
        ) VALUES (?, ?, ?, ?, ?)
        """, tenantId, appCode, clientId, allowedScopes, status);
  }

  private void insertApp(JdbcTemplate jdbcTemplate, String tenantId, String appCode,
      String authMode, String status) {
    jdbcTemplate.update("""
        INSERT INTO openapi_app (tenant_id, app_code, auth_mode, status)
        VALUES (?, ?, ?, ?)
        """, tenantId, appCode, authMode, status);
  }

  private JdbcTemplate createJdbcTemplate() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:openapi_oauth2_client_" + System.nanoTime()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa", "");
    dataSource.setDriverClassName("org.h2.Driver");
    return new JdbcTemplate(dataSource);
  }
}
