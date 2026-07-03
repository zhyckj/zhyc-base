/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 基于 JDBC 的开放 API 运行态限流策略仓储。
 */
public class JdbcOpenApiRateLimitPolicyRepository implements OpenApiRateLimitPolicyRepository {

  /** 限流策略查询 SQL。 */
  private static final String SELECT_SQL = """
      SELECT tenant_id, app_code, api_code, limit_count, window_seconds
      FROM openapi_rate_limit_policy
      WHERE tenant_id = ?
        AND app_code = ?
        AND api_code = ?
        AND status = 'enabled'
      LIMIT 1
      """;

  /** JDBC 操作模板。 */
  private final JdbcTemplate jdbcTemplate;

  /**
   * 创建 JDBC 限流策略仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   */
  public JdbcOpenApiRateLimitPolicyRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JDBC 操作模板不能为空");
  }

  /**
   * 查询启用状态的开放 API 限流策略。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param apiCode API 业务编码
   * @return 限流策略，不存在时为空
   */
  @Override
  public Optional<OpenApiRateLimitPolicy> findEnabledPolicy(String tenantId, String appCode,
      String apiCode) {
    List<OpenApiRateLimitPolicy> policies = jdbcTemplate.query(SELECT_SQL,
        (rs, rowNum) -> new OpenApiRateLimitPolicy(
            rs.getString("tenant_id"),
            rs.getString("app_code"),
            rs.getString("api_code"),
            rs.getInt("limit_count"),
            Duration.ofSeconds(rs.getInt("window_seconds"))),
        tenantId, appCode, apiCode);
    return policies.stream().findFirst();
  }
}
