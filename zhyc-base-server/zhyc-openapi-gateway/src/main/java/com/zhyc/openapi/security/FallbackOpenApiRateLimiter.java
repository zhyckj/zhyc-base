/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 带兜底能力的开放 API 限流器。
 *
 * <p>优先使用 Redis 等高速共享限流器，主限流器异常时回退到 JDBC 限流器，避免缓存中间件故障直接阻断开放 API。</p>
 */
public class FallbackOpenApiRateLimiter implements OpenApiRateLimiter {

  /** 限流兜底日志。 */
  private static final Logger LOGGER = LoggerFactory.getLogger(FallbackOpenApiRateLimiter.class);

  /** 主限流器。 */
  private final OpenApiRateLimiter primary;
  /** 兜底限流器。 */
  private final OpenApiRateLimiter fallback;

  /**
   * 创建带兜底能力的开放 API 限流器。
   *
   * @param primary 主限流器
   * @param fallback 兜底限流器
   */
  public FallbackOpenApiRateLimiter(OpenApiRateLimiter primary, OpenApiRateLimiter fallback) {
    this.primary = Objects.requireNonNull(primary, "主限流器不能为空");
    this.fallback = Objects.requireNonNull(fallback, "兜底限流器不能为空");
  }

  @Override
  public OpenApiRateLimitResult tryAcquire(OpenApiRateLimitPolicy policy, String tenantId,
      String appCode, String apiCode) {
    try {
      return primary.tryAcquire(policy, tenantId, appCode, apiCode);
    } catch (RuntimeException ex) {
      LOGGER.warn("Redis 开放 API 限流失败，已回退 JDBC 限流，tenantId={}, appCode={}, apiCode={}",
          tenantId, appCode, apiCode, ex);
      return fallback.tryAcquire(policy, tenantId, appCode, apiCode);
    }
  }
}
