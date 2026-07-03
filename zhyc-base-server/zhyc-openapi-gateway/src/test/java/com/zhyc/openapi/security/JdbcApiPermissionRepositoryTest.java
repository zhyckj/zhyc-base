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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JDBC 开放 API 权限仓储测试。
 */
class JdbcApiPermissionRepositoryTest {

  /**
   * 验证 JDBC 权限仓储可以匹配启用状态的精确路径授权。
   */
  @Test
  void shouldAllowEnabledExactPermission() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    insertPermission(jdbcTemplate, "tenant_a", "purchase-app", "POST",
        "/openapi/v1/purchase/requests", "enabled");
    ApiPermissionRepository repository = new JdbcApiPermissionRepository(jdbcTemplate);

    boolean allowed = repository.isAllowed("tenant_a", "purchase-app", "post",
        "/openapi/v1/purchase/requests");

    assertTrue(allowed);
  }

  /**
   * 验证 JDBC 权限仓储可以匹配通配路径授权。
   */
  @Test
  void shouldAllowEnabledWildcardPermission() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    insertPermission(jdbcTemplate, "tenant_a", "purchase-app", "GET",
        "/openapi/v1/purchase/requests/**", "enabled");
    ApiPermissionRepository repository = new JdbcApiPermissionRepository(jdbcTemplate);

    boolean allowed = repository.isAllowed("tenant_a", "purchase-app", "GET",
        "/openapi/v1/purchase/requests/1001");

    assertTrue(allowed);
  }

  /**
   * 验证 JDBC 权限仓储可以返回命中的 API 业务编码，供调用审计聚合使用。
   */
  @Test
  void shouldResolveMatchedApiCode() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    insertPermission(jdbcTemplate, "tenant_a", "purchase-app", "POST",
        "/openapi/v1/purchase/requests", "enabled");
    ApiPermissionRepository repository = new JdbcApiPermissionRepository(jdbcTemplate);

    String apiCode = repository.resolveApiCode("tenant_a", "purchase-app", "POST",
        "/openapi/v1/purchase/requests");

    assertEquals("purchase.request.create", apiCode);
  }

  /**
   * 验证采购订单详情开放 API 可以按租户、应用、方法和路径解析到对应 API 编码。
   */
  @Test
  void shouldResolvePurchaseOrderDetailPermission() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    insertPermission(jdbcTemplate, "tenant_a", "purchase-app", "purchase-order-detail",
        "GET", "/openapi/v1/purchase/orders/**", "enabled");
    ApiPermissionRepository repository = new JdbcApiPermissionRepository(jdbcTemplate);

    String apiCode = repository.resolveApiCode("tenant_a", "purchase-app", "GET",
        "/openapi/v1/purchase/orders/PO202606260001");

    assertTrue(repository.isAllowed("tenant_a", "purchase-app", "GET",
        "/openapi/v1/purchase/orders/PO202606260001"));
    assertEquals("purchase-order-detail", apiCode);
  }

  /**
   * 验证禁用授权和方法不匹配时不会放行。
   */
  @Test
  void shouldRejectDisabledOrMethodMismatchPermission() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTable(jdbcTemplate);
    insertPermission(jdbcTemplate, "tenant_a", "purchase-app", "POST",
        "/openapi/v1/purchase/requests", "disabled");
    ApiPermissionRepository repository = new JdbcApiPermissionRepository(jdbcTemplate);

    assertFalse(repository.isAllowed("tenant_a", "purchase-app", "POST",
        "/openapi/v1/purchase/requests"));
    assertFalse(repository.isAllowed("tenant_a", "purchase-app", "GET",
        "/openapi/v1/purchase/requests"));
  }

  private void createTable(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
        CREATE TABLE openapi_api_permission (
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            api_code VARCHAR(128) NOT NULL,
            http_method VARCHAR(16) NOT NULL,
            path_pattern VARCHAR(256) NOT NULL,
            status VARCHAR(32) NOT NULL
        )
        """);
  }

  private void insertPermission(JdbcTemplate jdbcTemplate, String tenantId, String appCode,
                                String httpMethod, String pathPattern, String status) {
    insertPermission(jdbcTemplate, tenantId, appCode, "purchase.request.create",
        httpMethod, pathPattern, status);
  }

  private void insertPermission(JdbcTemplate jdbcTemplate, String tenantId, String appCode,
                                String apiCode, String httpMethod, String pathPattern,
                                String status) {
    jdbcTemplate.update("""
        INSERT INTO openapi_api_permission (
            tenant_id, app_code, api_code, http_method, path_pattern, status
        ) VALUES (?, ?, ?, ?, ?, ?)
        """, tenantId, appCode, apiCode, httpMethod, pathPattern, status);
  }

  private JdbcTemplate createJdbcTemplate() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:openapi_permission_" + System.nanoTime()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa", "");
    dataSource.setDriverClassName("org.h2.Driver");
    return new JdbcTemplate(dataSource);
  }
}
