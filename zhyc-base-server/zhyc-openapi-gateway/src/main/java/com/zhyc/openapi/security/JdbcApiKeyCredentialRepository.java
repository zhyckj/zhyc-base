/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 JDBC 的 API Key 凭证仓储。
 */
public class JdbcApiKeyCredentialRepository implements ApiKeyCredentialRepository {

  /** API Key 查询 SQL。 */
  private static final String SELECT_BY_ACCESS_KEY = """
      SELECT api_key.tenant_id,
             api_key.app_code,
             api_key.access_key,
             api_key.secret_cipher,
             api_key.status,
             api_key.expire_at
      FROM openapi_api_key api_key
      INNER JOIN openapi_app app
        ON app.tenant_id = api_key.tenant_id
       AND app.app_code = api_key.app_code
      WHERE api_key.access_key = ?
        AND app.status = 'enabled'
        AND app.auth_mode IN ('api_key', 'both')
      """;

  /** JDBC 操作模板。 */
  private final JdbcTemplate jdbcTemplate;
  /** API Secret 解析器。 */
  private final ApiSecretResolver secretResolver;
  /** API Key 行映射器。 */
  private final RowMapper<ApiKeyCredential> rowMapper;

  /**
   * 创建 JDBC API Key 凭证仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @param secretResolver API Secret 解析器
   */
  public JdbcApiKeyCredentialRepository(JdbcTemplate jdbcTemplate, ApiSecretResolver secretResolver) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JDBC 操作模板不能为空");
    this.secretResolver = Objects.requireNonNull(secretResolver, "API Secret 解析器不能为空");
    this.rowMapper = (resultSet, rowNum) -> new ApiKeyCredential(
        resultSet.getString("tenant_id"),
        resultSet.getString("app_code"),
        resultSet.getString("access_key"),
        this.secretResolver.resolve(resultSet.getString("secret_cipher")),
        resultSet.getString("status"),
        toInstant(resultSet.getTimestamp("expire_at")));
  }

  /**
   * 按 API 访问密钥查询运行态凭证。
   *
   * @param accessKey API 访问密钥
   * @return API Key 运行态凭证
   */
  @Override
  public Optional<ApiKeyCredential> findByAccessKey(String accessKey) {
    List<ApiKeyCredential> credentials = jdbcTemplate.query(SELECT_BY_ACCESS_KEY, rowMapper, accessKey);
    return credentials.stream().findFirst();
  }

  private Instant toInstant(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toInstant();
  }
}
