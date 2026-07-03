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

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JDBC 开放 API 调用审计记录器测试。
 */
class JdbcApiCallAuditRecorderTest {

  /**
   * 验证 JDBC 调用审计记录器可以写入开放 API 调用审计表。
   */
  @Test
  void shouldRecordOpenApiCallAudit() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    jdbcTemplate.execute("""
        CREATE TABLE openapi_call_audit (
            tenant_id VARCHAR(64) NOT NULL,
            app_code VARCHAR(64) NOT NULL,
            access_key VARCHAR(128) NOT NULL,
            api_code VARCHAR(128) NOT NULL,
            http_method VARCHAR(16) NOT NULL,
            request_path VARCHAR(512) NOT NULL,
            response_status INT NOT NULL,
            duration_ms BIGINT NOT NULL,
            success TINYINT NOT NULL,
            error_code VARCHAR(64),
            client_ip VARCHAR(64) NOT NULL,
            request_id VARCHAR(128) NOT NULL,
            called_at TIMESTAMP NOT NULL
        )
        """);
    ApiCallAuditRecorder recorder = new JdbcApiCallAuditRecorder(jdbcTemplate);

    recorder.record(new ApiCallAuditRecord("tenant_a", "purchase-app", "AK123",
        "purchase.request.create", "post", "/openapi/v1/purchase/requests",
        201, 38L, true, null, "10.0.0.8", "req-001",
        Instant.parse("2026-06-24T04:45:00Z")));

    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM openapi_call_audit", Integer.class);
    String httpMethod = jdbcTemplate.queryForObject("SELECT http_method FROM openapi_call_audit", String.class);
    Integer success = jdbcTemplate.queryForObject("SELECT success FROM openapi_call_audit", Integer.class);
    Long durationMs = jdbcTemplate.queryForObject("SELECT duration_ms FROM openapi_call_audit", Long.class);

    assertEquals(1, count);
    assertEquals("POST", httpMethod);
    assertEquals(1, success);
    assertEquals(38L, durationMs);
  }

  private JdbcTemplate createJdbcTemplate() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:openapi_audit_" + System.nanoTime()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa", "");
    dataSource.setDriverClassName("org.h2.Driver");
    return new JdbcTemplate(dataSource);
  }
}
