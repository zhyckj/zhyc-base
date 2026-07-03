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
 * 基于 JDBC 的开放 API 运行态签名策略仓储。
 */
public class JdbcOpenApiSignaturePolicyRepository implements OpenApiSignaturePolicyRepository {

  /** 签名策略查询 SQL。 */
  private static final String SELECT_SQL = """
      SELECT tenant_id, app_code, algorithm, timestamp_tolerance_seconds,
             nonce_ttl_seconds, require_body_hash
      FROM openapi_signature_policy
      WHERE tenant_id = ?
        AND app_code = ?
        AND status = 'enabled'
      LIMIT 1
      """;

  /** JDBC 操作模板。 */
  private final JdbcTemplate jdbcTemplate;

  /**
   * 创建 JDBC 签名策略仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   */
  public JdbcOpenApiSignaturePolicyRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JDBC 操作模板不能为空");
  }

  /**
   * 查询启用状态的开放 API 签名策略。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开放 API 应用编码
   * @return 签名策略，不存在时为空
   */
  @Override
  public Optional<OpenApiSignaturePolicy> findEnabledPolicy(String tenantId, String appCode) {
    List<OpenApiSignaturePolicy> policies = jdbcTemplate.query(SELECT_SQL,
        (rs, rowNum) -> new OpenApiSignaturePolicy(
            rs.getString("tenant_id"),
            rs.getString("app_code"),
            rs.getString("algorithm"),
            Duration.ofSeconds(rs.getInt("timestamp_tolerance_seconds")),
            Duration.ofSeconds(rs.getInt("nonce_ttl_seconds")),
            rs.getInt("require_body_hash") == 1),
        tenantId, appCode);
    return policies.stream().findFirst();
  }
}
