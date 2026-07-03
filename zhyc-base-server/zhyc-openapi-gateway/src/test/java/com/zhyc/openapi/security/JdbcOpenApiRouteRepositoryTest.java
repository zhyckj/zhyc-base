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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JDBC 开放 API 路由仓储测试。
 */
class JdbcOpenApiRouteRepositoryTest {

  /**
   * 验证 JDBC 路由仓储可以按 HTTP 方法和路径解析已发布后端路由。
   */
  @Test
  void shouldResolvePublishedBackendRouteByMethodAndPath() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTables(jdbcTemplate);
    insertCatalog(jdbcTemplate, "purchase.request.create", "POST",
        "/openapi/v1/purchase/requests", "enabled");
    insertVersion(jdbcTemplate, "purchase.request.create", "v1",
        "http://purchase-service/internal/purchase/requests", "published");
    OpenApiRouteRepository repository = new JdbcOpenApiRouteRepository(jdbcTemplate);

    Optional<OpenApiRoute> route = repository.findRoute("post", "/openapi/v1/purchase/requests");

    assertTrue(route.isPresent());
    assertEquals("purchase.request.create", route.get().getApiCode());
    assertEquals("http://purchase-service/internal/purchase/requests", route.get().getBackendRoute());
  }

  /**
   * 验证运行态路由会把请求路径变量替换到后端转发地址。
   */
  @Test
  void shouldExpandBackendRoutePathVariablesFromRequestPath() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTables(jdbcTemplate);
    insertCatalog(jdbcTemplate, "purchase-request-status", "GET",
        "/openapi/v1/purchase/requests/{requestNo}/status", "enabled");
    insertVersion(jdbcTemplate, "purchase-request-status", "v1",
        "http://zhyc-platform-app/openapi/v1/purchase/requests/{requestNo}/status", "published");
    OpenApiRouteRepository repository = new JdbcOpenApiRouteRepository(jdbcTemplate);

    Optional<OpenApiRoute> route = repository.findRoute("GET",
        "/openapi/v1/purchase/requests/PR202606250001/status");

    assertTrue(route.isPresent());
    assertEquals("purchase-request-status", route.get().getApiCode());
    assertEquals("http://zhyc-platform-app/openapi/v1/purchase/requests/PR202606250001/status",
        route.get().getBackendRoute());
  }

  /**
   * 验证采购订单详情开放 API 可以通过网关运行态路由解析并替换订单号路径变量。
   */
  @Test
  void shouldResolvePurchaseOrderDetailRouteWithPathVariable() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTables(jdbcTemplate);
    insertCatalog(jdbcTemplate, "purchase-order-detail", "GET",
        "/openapi/v1/purchase/orders/{orderNo}", "enabled");
    insertVersion(jdbcTemplate, "purchase-order-detail", "v1",
        "http://zhyc-platform-app/openapi/v1/purchase/orders/{orderNo}", "published");
    OpenApiRouteRepository repository = new JdbcOpenApiRouteRepository(jdbcTemplate);

    Optional<OpenApiRoute> route = repository.findRoute("GET",
        "/openapi/v1/purchase/orders/PO202606260001");

    assertTrue(route.isPresent());
    assertEquals("purchase-order-detail", route.get().getApiCode());
    assertEquals("http://zhyc-platform-app/openapi/v1/purchase/orders/PO202606260001",
        route.get().getBackendRoute());
  }

  /**
   * 验证禁用目录和未发布版本不会生成运行态路由。
   */
  @Test
  void shouldIgnoreDisabledCatalogOrUnpublishedVersion() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createTables(jdbcTemplate);
    insertCatalog(jdbcTemplate, "purchase.request.create", "POST",
        "/openapi/v1/purchase/requests", "disabled");
    insertVersion(jdbcTemplate, "purchase.request.create", "v1",
        "http://purchase-service/internal/purchase/requests", "draft");
    OpenApiRouteRepository repository = new JdbcOpenApiRouteRepository(jdbcTemplate);

    Optional<OpenApiRoute> route = repository.findRoute("POST", "/openapi/v1/purchase/requests");

    assertTrue(route.isEmpty());
  }

  private void createTables(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
        CREATE TABLE openapi_catalog (
            api_code VARCHAR(128) NOT NULL,
            http_method VARCHAR(16) NOT NULL,
            path_pattern VARCHAR(256) NOT NULL,
            status VARCHAR(32) NOT NULL
        )
        """);
    jdbcTemplate.execute("""
        CREATE TABLE openapi_version (
            api_code VARCHAR(128) NOT NULL,
            version VARCHAR(32) NOT NULL,
            backend_route VARCHAR(512) NOT NULL,
            status VARCHAR(32) NOT NULL
        )
        """);
  }

  private void insertCatalog(JdbcTemplate jdbcTemplate, String apiCode, String httpMethod,
                             String pathPattern, String status) {
    jdbcTemplate.update("""
        INSERT INTO openapi_catalog (api_code, http_method, path_pattern, status)
        VALUES (?, ?, ?, ?)
        """, apiCode, httpMethod, pathPattern, status);
  }

  private void insertVersion(JdbcTemplate jdbcTemplate, String apiCode, String version,
                             String backendRoute, String status) {
    jdbcTemplate.update("""
        INSERT INTO openapi_version (api_code, version, backend_route, status)
        VALUES (?, ?, ?, ?)
        """, apiCode, version, backendRoute, status);
  }

  private JdbcTemplate createJdbcTemplate() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:openapi_route_" + System.nanoTime()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa", "");
    dataSource.setDriverClassName("org.h2.Driver");
    return new JdbcTemplate(dataSource);
  }
}
