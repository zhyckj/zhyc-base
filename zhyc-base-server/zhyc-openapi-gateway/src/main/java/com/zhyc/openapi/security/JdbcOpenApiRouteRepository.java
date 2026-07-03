/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 JDBC 的开放 API 运行态路由仓储。
 */
public class JdbcOpenApiRouteRepository implements OpenApiRouteRepository {

  /** 已发布 API 路由查询 SQL。 */
  private static final String SELECT_PUBLISHED_ROUTES = """
      SELECT c.api_code, c.path_pattern, v.backend_route
      FROM openapi_catalog c
      JOIN openapi_version v ON v.api_code = c.api_code
      WHERE c.http_method = ?
        AND c.status = 'enabled'
        AND v.status = 'published'
      ORDER BY v.version DESC
      """;

  /** JDBC 操作模板。 */
  private final JdbcTemplate jdbcTemplate;
  /** 请求路径匹配器。 */
  private final PathMatcher pathMatcher;

  /**
   * 创建 JDBC 开放 API 运行态路由仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   */
  public JdbcOpenApiRouteRepository(JdbcTemplate jdbcTemplate) {
    this(jdbcTemplate, new AntPathMatcher());
  }

  /**
   * 创建 JDBC 开放 API 运行态路由仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @param pathMatcher 请求路径匹配器
   */
  public JdbcOpenApiRouteRepository(JdbcTemplate jdbcTemplate, PathMatcher pathMatcher) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JDBC 操作模板不能为空");
    this.pathMatcher = Objects.requireNonNull(pathMatcher, "请求路径匹配器不能为空");
  }

  /**
   * 查询请求命中的已发布开放 API 路由。
   *
   * @param httpMethod HTTP 方法
   * @param requestPath 请求路径
   * @return 运行态路由，不存在时为空
   */
  @Override
  public Optional<OpenApiRoute> findRoute(String httpMethod, String requestPath) {
    if (isBlank(httpMethod) || isBlank(requestPath)) {
      return Optional.empty();
    }
    List<RouteCandidate> routes = jdbcTemplate.query(SELECT_PUBLISHED_ROUTES,
        (rs, rowNum) -> new RouteCandidate(rs.getString("api_code"),
            rs.getString("path_pattern"), rs.getString("backend_route")),
        httpMethod.trim().toUpperCase(Locale.ROOT));
    return routes.stream()
        .filter(route -> !isBlank(route.pathPattern()) && !isBlank(route.backendRoute()))
        .filter(route -> pathMatcher.match(route.pathPattern(), requestPath.trim()))
        .map(route -> new OpenApiRoute(route.apiCode(),
            expandBackendRoute(route.pathPattern(), requestPath.trim(), route.backendRoute())))
        .findFirst();
  }

  /**
   * 将请求路径变量替换到后端转发路由。
   *
   * @param pathPattern 开放 API 请求路径匹配规则
   * @param requestPath 调用方实际请求路径
   * @param backendRoute 后端转发路由模板
   * @return 已替换路径变量的后端转发路由
   */
  private String expandBackendRoute(String pathPattern, String requestPath, String backendRoute) {
    Map<String, String> pathVariables = pathMatcher.extractUriTemplateVariables(pathPattern, requestPath);
    String expandedRoute = backendRoute;
    for (Map.Entry<String, String> entry : pathVariables.entrySet()) {
      expandedRoute = expandedRoute.replace("{" + entry.getKey() + "}", entry.getValue());
    }
    return expandedRoute;
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  /**
   * 开放 API 路由匹配候选。
   *
   * @param apiCode API 业务编码
   * @param pathPattern 请求路径匹配规则
   * @param backendRoute 后端转发路由
   */
  private record RouteCandidate(String apiCode, String pathPattern, String backendRoute) {
  }
}
