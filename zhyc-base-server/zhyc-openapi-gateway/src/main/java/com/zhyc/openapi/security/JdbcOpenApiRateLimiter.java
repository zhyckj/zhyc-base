/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 基于 JDBC 固定窗口的开放 API 限流器。
 *
 * <p>通过数据库唯一键聚合同一租户、应用、API 和时间窗口的请求计数，支持多网关实例共享限流状态。</p>
 */
public class JdbcOpenApiRateLimiter implements OpenApiRateLimiter {

  /** 限流拒绝错误编码。 */
  public static final String ERROR_RATE_LIMITED = "RATE_LIMITED";

  /** 删除过期限流计数 SQL。 */
  private static final String DELETE_EXPIRED_SQL = """
      DELETE FROM openapi_rate_limit_counter
      WHERE expires_at <= ?
      """;

  /** 新增窗口计数 SQL。 */
  private static final String INSERT_SQL = """
      INSERT INTO openapi_rate_limit_counter (
          tenant_id, app_code, api_code, window_seconds, window_index,
          request_count, expires_at, created_at, updated_at
      ) VALUES (?, ?, ?, ?, ?, 1, ?, ?, ?)
      """;

  /** 累加窗口计数 SQL。 */
  private static final String UPDATE_SQL = """
      UPDATE openapi_rate_limit_counter
      SET request_count = request_count + 1,
          updated_at = ?
      WHERE tenant_id = ?
        AND app_code = ?
        AND api_code = ?
        AND window_seconds = ?
        AND window_index = ?
      """;

  /** 查询窗口计数 SQL。 */
  private static final String SELECT_COUNT_SQL = """
      SELECT request_count
      FROM openapi_rate_limit_counter
      WHERE tenant_id = ?
        AND app_code = ?
        AND api_code = ?
        AND window_seconds = ?
        AND window_index = ?
      """;

  /** JDBC 操作模板。 */
  private final JdbcTemplate jdbcTemplate;
  /** 运行期时钟。 */
  private final Clock clock;

  /**
   * 创建 JDBC 固定窗口限流器。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @param clock 运行期时钟
   */
  public JdbcOpenApiRateLimiter(JdbcTemplate jdbcTemplate, Clock clock) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "JDBC 操作模板不能为空");
    this.clock = Objects.requireNonNull(clock, "运行期时钟不能为空");
  }

  /**
   * 尝试占用一次 JDBC 共享限流配额。
   *
   * @param policy 限流策略
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param apiCode API 业务编码
   * @return 限流判定结果
   */
  @Override
  public OpenApiRateLimitResult tryAcquire(OpenApiRateLimitPolicy policy, String tenantId,
      String appCode, String apiCode) {
    Objects.requireNonNull(policy, "开放 API 限流策略不能为空");
    WindowKey windowKey = buildWindowKey(policy, tenantId, appCode, apiCode);
    Instant now = clock.instant();
    jdbcTemplate.update(DELETE_EXPIRED_SQL, Timestamp.from(now));
    int current = incrementAndGet(windowKey, now);
    if (current > policy.getLimitCount()) {
      return OpenApiRateLimitResult.rejected(ERROR_RATE_LIMITED,
          Math.max(1, windowKey.expiresAt().getEpochSecond() - now.getEpochSecond()));
    }
    return OpenApiRateLimitResult.allowed();
  }

  /**
   * 累加并读取当前窗口计数。
   *
   * @param windowKey 限流窗口键
   * @param now 当前时间
   * @return 当前窗口请求次数
   */
  private int incrementAndGet(WindowKey windowKey, Instant now) {
    int updatedRows = jdbcTemplate.update(UPDATE_SQL, Timestamp.from(now), windowKey.tenantId(),
        windowKey.appCode(), windowKey.apiCode(), windowKey.windowSeconds(), windowKey.windowIndex());
    if (updatedRows == 0) {
      insertWindowCounter(windowKey, now);
    }
    Integer count = jdbcTemplate.queryForObject(SELECT_COUNT_SQL, Integer.class,
        windowKey.tenantId(), windowKey.appCode(), windowKey.apiCode(), windowKey.windowSeconds(),
        windowKey.windowIndex());
    return count == null ? 0 : count;
  }

  /**
   * 写入当前限流窗口首条计数。
   *
   * @param windowKey 限流窗口键
   * @param now 当前时间
   */
  private void insertWindowCounter(WindowKey windowKey, Instant now) {
    try {
      jdbcTemplate.update(INSERT_SQL, windowKey.tenantId(), windowKey.appCode(),
          windowKey.apiCode(), windowKey.windowSeconds(), windowKey.windowIndex(),
          Timestamp.from(windowKey.expiresAt()), Timestamp.from(now), Timestamp.from(now));
    } catch (DuplicateKeyException ex) {
      jdbcTemplate.update(UPDATE_SQL, Timestamp.from(now), windowKey.tenantId(),
          windowKey.appCode(), windowKey.apiCode(), windowKey.windowSeconds(),
          windowKey.windowIndex());
    }
  }

  /**
   * 构造固定窗口键。
   *
   * @param policy 限流策略
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param apiCode API 业务编码
   * @return 固定窗口键
   */
  private WindowKey buildWindowKey(OpenApiRateLimitPolicy policy, String tenantId, String appCode,
      String apiCode) {
    long windowSeconds = policy.getWindow().toSeconds();
    long windowIndex = clock.instant().getEpochSecond() / windowSeconds;
    Instant expiresAt = Instant.ofEpochSecond((windowIndex + 1) * windowSeconds);
    return new WindowKey(tenantId, appCode, apiCode, windowSeconds, windowIndex, expiresAt);
  }

  /**
   * 开放 API 限流固定窗口键。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param apiCode API 业务编码
   * @param windowSeconds 窗口秒数
   * @param windowIndex 窗口序号
   * @param expiresAt 窗口过期时间
   */
  private record WindowKey(String tenantId, String appCode, String apiCode, long windowSeconds,
      long windowIndex, Instant expiresAt) {
  }
}
