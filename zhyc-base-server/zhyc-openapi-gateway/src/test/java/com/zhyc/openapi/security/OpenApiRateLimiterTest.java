/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/**
 * 开放 API 固定窗口限流器测试。
 */
class OpenApiRateLimiterTest {

  /** 固定测试时钟。 */
  private final Clock clock = Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC);

  /**
   * 验证固定窗口限流器允许窗口内前 N 次请求，并拒绝第 N+1 次。
   */
  @Test
  void shouldRejectRequestWhenFixedWindowQuotaExceeded() {
    OpenApiRateLimiter limiter = new InMemoryOpenApiRateLimiter(clock);
    OpenApiRateLimitPolicy policy = new OpenApiRateLimitPolicy("tenant_a", "purchase-app",
        "purchase.request.create", 2, Duration.ofMinutes(1));

    assertTrue(limiter.tryAcquire(policy, "tenant_a", "purchase-app", "purchase.request.create").isAllowed());
    assertTrue(limiter.tryAcquire(policy, "tenant_a", "purchase-app", "purchase.request.create").isAllowed());
    OpenApiRateLimitResult third = limiter.tryAcquire(policy, "tenant_a", "purchase-app",
        "purchase.request.create");

    assertEquals(false, third.isAllowed());
    assertEquals("RATE_LIMITED", third.getFailureCode());
    assertEquals(60, third.getRetryAfterSeconds());
  }
}
