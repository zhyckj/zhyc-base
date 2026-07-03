/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * Redis 开放 API 限流器测试。
 */
class RedisOpenApiRateLimiterTest {

  /** 固定测试时钟。 */
  private final Clock clock = Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC);

  /**
   * 验证 Redis 限流器使用脚本计数，并在超过策略阈值时返回限流结果。
   */
  @Test
  void shouldRejectWhenRedisCounterExceedsLimit() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    when(redisTemplate.execute(any(RedisScript.class), anyList(), eq("61"))).thenReturn(3L);
    RedisOpenApiRateLimiter limiter = new RedisOpenApiRateLimiter(redisTemplate, clock, "zhyc-test");
    OpenApiRateLimitPolicy policy = new OpenApiRateLimitPolicy("tenant_a", "app_a", "api_a", 2,
        Duration.ofMinutes(1));

    OpenApiRateLimitResult result = limiter.tryAcquire(policy, "tenant_a", "app_a", "api_a");

    assertFalse(result.isAllowed());
    assertEquals("RATE_LIMITED", result.getFailureCode());
    assertEquals(60, result.getRetryAfterSeconds());
  }

  /**
   * 验证主限流器异常时会回退到兜底限流器。
   */
  @Test
  void fallbackLimiterShouldUseFallbackWhenPrimaryFails() {
    OpenApiRateLimiter primary = (policy, tenantId, appCode, apiCode) -> {
      throw new IllegalStateException("redis unavailable");
    };
    OpenApiRateLimiter fallback = (policy, tenantId, appCode, apiCode) -> OpenApiRateLimitResult.allowed();
    FallbackOpenApiRateLimiter limiter = new FallbackOpenApiRateLimiter(primary, fallback);
    OpenApiRateLimitPolicy policy = new OpenApiRateLimitPolicy("tenant_a", "app_a", "api_a", 1,
        Duration.ofMinutes(1));

    assertTrue(limiter.tryAcquire(policy, "tenant_a", "app_a", "api_a").isAllowed());
  }
}
