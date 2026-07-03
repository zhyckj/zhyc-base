/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 基于 JDBC 的开放平台 OAuth2 客户端运行态映射仓储。
 */
public class JdbcOpenApiOAuth2ClientMappingRepository implements OpenApiOAuth2ClientMappingRepository {

  /** OAuth2 客户端映射查询 SQL。 */
  private static final String SELECT_SQL = """
      SELECT oauth_client.tenant_id,
             oauth_client.app_code,
             oauth_client.client_id,
             oauth_client.allowed_scopes
      FROM openapi_oauth_client oauth_client
      INNER JOIN openapi_app app
        ON app.tenant_id = oauth_client.tenant_id
       AND app.app_code = oauth_client.app_code
      WHERE oauth_client.tenant_id = ?
        AND oauth_client.client_id = ?
        AND oauth_client.status = 'enabled'
        AND app.status = 'enabled'
        AND app.auth_mode IN ('oauth2', 'both')
      LIMIT 1
      """;

  /** JDBC 操作模板。 */
  private final JdbcTemplate jdbcTemplate;

  /**
   * 创建 JDBC OAuth2 客户端映射仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   */
  public JdbcOpenApiOAuth2ClientMappingRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JDBC 操作模板不能为空");
  }

  /**
   * 查询启用状态的 OAuth2 客户端与开放 API 应用映射。
   *
   * @param tenantId 租户业务编码
   * @param clientId OAuth2 客户端 ID
   * @return OAuth2 客户端运行态映射
   */
  @Override
  public Optional<OpenApiOAuth2ClientMapping> findEnabledByTenantIdAndClientId(
      String tenantId, String clientId) {
    List<OpenApiOAuth2ClientMapping> mappings = jdbcTemplate.query(SELECT_SQL,
        (rs, rowNum) -> new OpenApiOAuth2ClientMapping(
            rs.getString("tenant_id"),
            rs.getString("app_code"),
            rs.getString("client_id"),
            rs.getString("allowed_scopes")),
        tenantId, clientId);
    return mappings.stream().findFirst();
  }
}
