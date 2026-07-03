/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.audit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * 认证中心登录审计服务单元测试。
 *
 * <p>验证统一认证登录成功和失败都会按平台日志表口径写入审计数据。</p>
 */
class AuthLoginAuditServiceTest {

  /**
   * 验证登录成功时写入平台映射账号，而不是认证中心内部登录账号。
   */
  @Test
  void shouldRecordSuccessWithPlatformUser() {
    JdbcOperations jdbcOperations = Mockito.mock(JdbcOperations.class);
    AuthLoginAuditService service = new AuthLoginAuditService(
        jdbcOperations, "tenant_a", 1001L, "platform_admin");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
    request.addHeader("User-Agent", "Chrome");

    service.recordSuccess(
        UsernamePasswordAuthenticationToken.authenticated("auth-admin", "N/A", java.util.List.of()), request);

    verify(jdbcOperations).update(
        contains("INSERT INTO sys_login_log"),
        eq("tenant_a"),
        eq(1001L),
        eq("platform_admin"),
        eq("password"),
        eq("success"),
        eq("10.0.0.1"),
        eq("Chrome"));
  }

  /**
   * 验证登录失败时写入提交账号和失败结果，用户主键保持为空。
   */
  @Test
  void shouldRecordFailureWithSubmittedUsername() {
    JdbcOperations jdbcOperations = Mockito.mock(JdbcOperations.class);
    AuthLoginAuditService service = new AuthLoginAuditService(
        jdbcOperations, "tenant_a", 1001L, "platform_admin");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("127.0.0.1");

    service.recordFailure("wrong-user", request, new BadCredentialsException("bad credentials"));

    verify(jdbcOperations).update(
        contains("INSERT INTO sys_login_log"),
        eq("tenant_a"),
        eq(null),
        eq("wrong-user"),
        eq("password"),
        eq("failure"),
        eq("127.0.0.1"),
        eq(null));
  }

  /**
   * 验证未配置认证中心数据源时审计能力自动降级，不影响认证流程。
   */
  @Test
  void shouldSkipAuditWhenJdbcIsNotConfigured() {
    AuthLoginAuditService service = new AuthLoginAuditService(null, "tenant_a", 1001L, "platform_admin");

    assertDoesNotThrow(() -> service.recordSuccess(
        UsernamePasswordAuthenticationToken.authenticated("auth-admin", "N/A", java.util.List.of()),
        new MockHttpServletRequest()));
  }
}
