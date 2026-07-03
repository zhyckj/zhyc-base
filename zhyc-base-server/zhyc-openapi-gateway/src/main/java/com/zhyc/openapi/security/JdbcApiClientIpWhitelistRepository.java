/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Objects;

/**
 * 基于 JDBC 的开放 API 应用客户端 IP 白名单仓储。
 *
 * <p>从 {@code openapi_app.ip_whitelist} 读取 JSON 字符串数组配置；未配置白名单时默认放行，
 * 已配置时只允许命中 IPv4 或 IPv4 CIDR 的客户端 IP。</p>
 */
public class JdbcApiClientIpWhitelistRepository implements ApiClientIpWhitelistRepository {

  /** 查询启用应用 IP 白名单 SQL。 */
  private static final String SELECT_APP_IP_WHITELIST = """
      SELECT ip_whitelist
      FROM openapi_app
      WHERE tenant_id = ?
        AND app_code = ?
        AND status = 'enabled'
      """;

  /** JDBC 操作模板。 */
  private final JdbcTemplate jdbcTemplate;
  /** IP 白名单匹配器。 */
  private final OpenApiClientIpMatcher clientIpMatcher;

  /**
   * 创建 JDBC 应用客户端 IP 白名单仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   */
  public JdbcApiClientIpWhitelistRepository(JdbcTemplate jdbcTemplate) {
    this(jdbcTemplate, new OpenApiClientIpMatcher());
  }

  /**
   * 创建 JDBC 应用客户端 IP 白名单仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @param clientIpMatcher IP 白名单匹配器
   */
  public JdbcApiClientIpWhitelistRepository(JdbcTemplate jdbcTemplate,
      OpenApiClientIpMatcher clientIpMatcher) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JDBC 操作模板不能为空");
    this.clientIpMatcher = Objects.requireNonNull(clientIpMatcher, "IP 白名单匹配器不能为空");
  }

  /**
   * 判断客户端 IP 是否允许访问指定应用。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param clientIp 客户端 IP
   * @return 白名单允许时返回 {@code true}
   */
  @Override
  public boolean isAllowed(String tenantId, String appCode, String clientIp) {
    if (isBlank(tenantId) || isBlank(appCode) || isBlank(clientIp)) {
      return false;
    }
    List<String> whitelists = jdbcTemplate.query(SELECT_APP_IP_WHITELIST,
        (rs, rowNum) -> rs.getString("ip_whitelist"), tenantId.trim(), appCode.trim());
    if (whitelists.isEmpty()) {
      return false;
    }
    String ipWhitelist = whitelists.get(0);
    if (isBlank(ipWhitelist)) {
      return true;
    }
    return clientIpMatcher.matches(ipWhitelist, clientIp.trim());
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
