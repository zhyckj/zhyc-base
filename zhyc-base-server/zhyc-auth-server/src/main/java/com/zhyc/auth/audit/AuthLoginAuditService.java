/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.audit;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 认证中心登录审计服务。
 *
 * <p>认证中心保持独立模块边界，不直接依赖系统模块 Service；本服务通过受控 JDBC 写入平台登录日志表，
 * 审计写入失败时只记录告警，避免影响用户登录主流程。</p>
 */
public class AuthLoginAuditService {

  /** 登录成功结果编码，与系统模块登录日志字典保持一致。 */
  private static final String RESULT_SUCCESS = "success";

  /** 登录失败结果编码，与系统模块登录日志字典保持一致。 */
  private static final String RESULT_FAILURE = "failure";

  /** 密码登录类型编码，用于区分后续短信、三方登录、扫码登录等方式。 */
  private static final String LOGIN_TYPE_PASSWORD = "password";

  /** 登录日志写入 SQL，字段显式列出以兼容 MySQL 5.7。 */
  private static final String INSERT_LOGIN_LOG_SQL = """
      INSERT INTO sys_login_log
        (tenant_id, user_id, username, login_type, result, client_ip, user_agent, created_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
      """;

  /** 日志对象，用于记录审计写入异常。 */
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthLoginAuditService.class);

  /** 认证中心数据库操作对象；未配置数据源时为空。 */
  private final JdbcOperations jdbcOperations;

  /** 平台租户编码，用于统一认证登录日志的租户隔离。 */
  private final String platformTenantId;

  /** 平台用户主键，用于登录成功后映射到后台管理用户。 */
  private final Long platformUserId;

  /** 平台登录账号，用于登录成功后展示业务账号。 */
  private final String platformUsername;

  /**
   * 创建认证中心登录审计服务。
   *
   * @param jdbcOperations JDBC 操作对象，允许为空以支持本地内存模式
   * @param platformTenantId 平台租户编码
   * @param platformUserId 平台用户主键
   * @param platformUsername 平台登录账号
   */
  public AuthLoginAuditService(
      JdbcOperations jdbcOperations,
      String platformTenantId,
      Long platformUserId,
      String platformUsername) {
    this.jdbcOperations = jdbcOperations;
    this.platformTenantId = requireText(platformTenantId, "认证中心平台租户编码不能为空");
    this.platformUserId = Objects.requireNonNull(platformUserId, "认证中心平台用户主键不能为空");
    this.platformUsername = requireText(platformUsername, "认证中心平台登录账号不能为空");
  }

  /**
   * 记录认证中心登录成功日志。
   *
   * <p>成功日志写入平台映射用户信息，保证后台登录日志展示的是业务账号，而不是认证中心内部账号。</p>
   *
   * @param authentication Spring Security 认证结果
   * @param request 当前登录请求
   */
  public void recordSuccess(Authentication authentication, HttpServletRequest request) {
    String fallbackUsername = authentication == null ? null : authentication.getName();
    record(platformUserId, firstText(platformUsername, fallbackUsername), RESULT_SUCCESS, request);
  }

  /**
   * 记录认证中心登录失败日志。
   *
   * <p>失败日志无法可信映射平台用户主键，只记录提交账号、客户端 IP 和 User-Agent，便于排查暴力尝试。</p>
   *
   * @param username 登录表单提交账号
   * @param request 当前登录请求
   * @param exception 认证失败异常
   */
  public void recordFailure(String username, HttpServletRequest request, AuthenticationException exception) {
    record(null, trimToNull(username), RESULT_FAILURE, request);
  }

  /**
   * 写入登录审计日志。
   *
   * <p>审计是旁路能力，数据库未配置、表未初始化或短暂异常时不得阻断认证中心登录。</p>
   *
   * @param userId 平台用户主键，登录失败时为空
   * @param username 登录账号
   * @param result 登录结果编码
   * @param request 当前登录请求
   */
  private void record(Long userId, String username, String result, HttpServletRequest request) {
    if (jdbcOperations == null) {
      return;
    }
    try {
      jdbcOperations.update(
          INSERT_LOGIN_LOG_SQL,
          platformTenantId,
          userId,
          limit(username, 64),
          LOGIN_TYPE_PASSWORD,
          result,
          limit(resolveClientIp(request), 64),
          limit(resolveUserAgent(request), 512));
    } catch (RuntimeException ex) {
      LOGGER.warn("认证中心登录审计写入失败，result={}", result, ex);
    }
  }

  /**
   * 解析客户端真实 IP。
   *
   * @param request 当前 HTTP 请求
   * @return 客户端 IP，无法识别时返回空
   */
  private static String resolveClientIp(HttpServletRequest request) {
    if (request == null) {
      return null;
    }
    String forwardedFor = trimToNull(request.getHeader("X-Forwarded-For"));
    if (forwardedFor != null) {
      int commaIndex = forwardedFor.indexOf(',');
      return commaIndex >= 0 ? forwardedFor.substring(0, commaIndex).trim() : forwardedFor;
    }
    String realIp = trimToNull(request.getHeader("X-Real-IP"));
    return realIp == null ? trimToNull(request.getRemoteAddr()) : realIp;
  }

  /**
   * 读取客户端 User-Agent。
   *
   * @param request 当前 HTTP 请求
   * @return User-Agent，无法识别时返回空
   */
  private static String resolveUserAgent(HttpServletRequest request) {
    return request == null ? null : trimToNull(request.getHeader("User-Agent"));
  }

  /**
   * 返回第一个非空文本。
   *
   * @param first 优先文本
   * @param second 兜底文本
   * @return 非空文本或空
   */
  private static String firstText(String first, String second) {
    String normalizedFirst = trimToNull(first);
    return normalizedFirst == null ? trimToNull(second) : normalizedFirst;
  }

  /**
   * 按数据库字段长度截断文本。
   *
   * @param value 原始文本
   * @param maxLength 最大长度
   * @return 截断后的文本
   */
  private static String limit(String value, int maxLength) {
    String normalizedValue = trimToNull(value);
    if (normalizedValue == null || normalizedValue.length() <= maxLength) {
      return normalizedValue;
    }
    return normalizedValue.substring(0, maxLength);
  }

  /**
   * 校验文本不能为空。
   *
   * @param value 原始文本
   * @param message 异常提示
   * @return 去除首尾空白后的文本
   */
  private static String requireText(String value, String message) {
    String normalizedValue = trimToNull(value);
    if (normalizedValue == null) {
      throw new IllegalStateException(message);
    }
    return normalizedValue;
  }

  /**
   * 规整可空文本。
   *
   * @param value 原始文本
   * @return 去除首尾空白后的文本；空白文本返回空
   */
  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmedValue = value.trim();
    return trimmedValue.isEmpty() ? null : trimmedValue;
  }
}
