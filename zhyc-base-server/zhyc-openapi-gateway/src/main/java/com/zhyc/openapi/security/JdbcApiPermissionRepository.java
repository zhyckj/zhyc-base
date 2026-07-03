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
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 JDBC 的开放 API 运行态权限仓储。
 */
public class JdbcApiPermissionRepository implements ApiPermissionRepository {

  /** 启用授权路径查询 SQL。 */
  private static final String SELECT_ENABLED_PERMISSIONS = """
      SELECT api_code, path_pattern
      FROM openapi_api_permission
      WHERE tenant_id = ?
        AND app_code = ?
        AND http_method = ?
        AND status = 'enabled'
      """;

  /** JDBC 操作模板。 */
  private final JdbcTemplate jdbcTemplate;
  /** 请求路径匹配器。 */
  private final PathMatcher pathMatcher;

  /**
   * 创建 JDBC 开放 API 运行态权限仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   */
  public JdbcApiPermissionRepository(JdbcTemplate jdbcTemplate) {
    this(jdbcTemplate, new AntPathMatcher());
  }

  /**
   * 创建 JDBC 开放 API 运行态权限仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @param pathMatcher 请求路径匹配器
   */
  public JdbcApiPermissionRepository(JdbcTemplate jdbcTemplate, PathMatcher pathMatcher) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JDBC 操作模板不能为空");
    this.pathMatcher = Objects.requireNonNull(pathMatcher, "请求路径匹配器不能为空");
  }

  /**
   * 判断应用是否被授权访问指定开放 API 路径。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param httpMethod HTTP 方法
   * @param requestPath 请求路径
   * @return 授权命中时返回 {@code true}
   */
  @Override
  public boolean isAllowed(String tenantId, String appCode, String httpMethod, String requestPath) {
    return findMatchedApiCode(tenantId, appCode, httpMethod, requestPath).isPresent();
  }

  /**
   * 解析请求路径命中的 API 业务编码。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param httpMethod HTTP 方法
   * @param requestPath 请求路径
   * @return 命中的 API 业务编码，未命中时返回请求路径
   */
  @Override
  public String resolveApiCode(String tenantId, String appCode, String httpMethod, String requestPath) {
    return findMatchedApiCode(tenantId, appCode, httpMethod, requestPath).orElse(requestPath);
  }

  /**
   * 查询当前请求命中的启用授权 API 编码。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param httpMethod HTTP 方法
   * @param requestPath 请求路径
   * @return 命中的 API 编码
   */
  private Optional<String> findMatchedApiCode(String tenantId, String appCode, String httpMethod,
                                              String requestPath) {
    if (isBlank(tenantId) || isBlank(appCode) || isBlank(httpMethod) || isBlank(requestPath)) {
      return Optional.empty();
    }
    List<PermissionPattern> permissions = jdbcTemplate.query(SELECT_ENABLED_PERMISSIONS,
        (rs, rowNum) -> new PermissionPattern(rs.getString("api_code"), rs.getString("path_pattern")),
        tenantId.trim(), appCode.trim(), httpMethod.trim().toUpperCase(Locale.ROOT));
    return permissions.stream()
        .filter(permission -> !isBlank(permission.pathPattern()))
        .filter(permission -> pathMatcher.match(permission.pathPattern(), requestPath.trim()))
        .map(PermissionPattern::apiCode)
        .filter(apiCode -> !isBlank(apiCode))
        .findFirst();
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  /**
   * 开放 API 授权路径匹配候选。
   *
   * @param apiCode API 业务编码
   * @param pathPattern 授权路径表达式
   */
  private record PermissionPattern(String apiCode, String pathPattern) {
  }
}
