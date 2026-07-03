/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * 基于 JDBC 的开放 API 调用审计记录器。
 */
public class JdbcApiCallAuditRecorder implements ApiCallAuditRecorder {

  /** 开放 API 调用审计写入 SQL。 */
  private static final String INSERT_AUDIT = """
      INSERT INTO openapi_call_audit (
          tenant_id, app_code, access_key, api_code, http_method, request_path,
          response_status, duration_ms, success, error_code, client_ip, request_id, called_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  /** JDBC 操作模板。 */
  private final JdbcTemplate jdbcTemplate;

  /**
   * 创建 JDBC 开放 API 调用审计记录器。
   *
   * @param jdbcTemplate JDBC 操作模板
   */
  public JdbcApiCallAuditRecorder(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JDBC 操作模板不能为空");
  }

  /**
   * 写入开放 API 调用审计记录。
   *
   * @param record 调用审计记录，包含租户、应用、API、结果、耗时、客户端 IP 和请求追踪 ID
   */
  @Override
  public void record(ApiCallAuditRecord record) {
    Objects.requireNonNull(record, "开放 API 调用审计记录不能为空");
    jdbcTemplate.update(INSERT_AUDIT,
        requireText(record.getTenantId(), "租户业务编码不能为空"),
        requireText(record.getAppCode(), "开发者应用编码不能为空"),
        requireText(record.getAccessKey(), "API 访问密钥不能为空"),
        requireText(record.getApiCode(), "API 业务编码不能为空"),
        requireText(record.getHttpMethod(), "HTTP 方法不能为空").toUpperCase(Locale.ROOT),
        requireText(record.getRequestPath(), "请求路径不能为空"),
        record.getResponseStatus(),
        record.getDurationMs(),
        record.isSuccess() ? 1 : 0,
        trimToNull(record.getErrorCode()),
        requireText(record.getClientIp(), "客户端 IP 不能为空"),
        requireText(record.getRequestId(), "请求追踪 ID 不能为空"),
        Timestamp.from(record.getCalledAt() == null ? Instant.now() : record.getCalledAt()));
  }

  private String requireText(String value, String message) {
    String normalized = trimToNull(value);
    if (normalized == null) {
      throw new IllegalArgumentException(message);
    }
    return normalized;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
