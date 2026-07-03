/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于内存固定窗口的开放 API 限流器。
 *
 * <p>首期用于单节点快速部署。集群部署时应替换为 Redis、网关集群限流或服务网格限流实现。</p>
 */
public class InMemoryOpenApiRateLimiter implements OpenApiRateLimiter {

  /** 限流拒绝错误编码。 */
  public static final String ERROR_RATE_LIMITED = "RATE_LIMITED";

  /** 运行期时钟。 */
  private final Clock clock;
  /** 固定窗口计数器。 */
  private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

  /**
   * 创建内存固定窗口限流器。
   *
   * @param clock 运行期时钟
   */
  public InMemoryOpenApiRateLimiter(Clock clock) {
    this.clock = Objects.requireNonNull(clock, "运行期时钟不能为空");
  }

  /**
   * 尝试占用一次本地内存限流配额。
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
    String key = buildWindowKey(policy, tenantId, appCode, apiCode);
    int current = counters.computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet();
    if (current > policy.getLimitCount()) {
      return OpenApiRateLimitResult.rejected(ERROR_RATE_LIMITED,
          retryAfterSeconds(policy));
    }
    return OpenApiRateLimitResult.allowed();
  }

  /**
   * 计算当前固定窗口剩余秒数。
   *
   * @param policy 限流策略
   * @return 距离下一窗口开始的秒数
   */
  private long retryAfterSeconds(OpenApiRateLimitPolicy policy) {
    long windowSeconds = policy.getWindow().toSeconds();
    long epochSecond = clock.instant().getEpochSecond();
    long windowEndSecond = (epochSecond / windowSeconds + 1) * windowSeconds;
    return Math.max(1, windowEndSecond - epochSecond);
  }

  private String buildWindowKey(OpenApiRateLimitPolicy policy, String tenantId, String appCode,
      String apiCode) {
    long windowSeconds = policy.getWindow().toSeconds();
    long windowIndex = clock.instant().getEpochSecond() / windowSeconds;
    return tenantId + ':' + appCode + ':' + apiCode + ':' + windowSeconds + ':' + windowIndex;
  }
}
