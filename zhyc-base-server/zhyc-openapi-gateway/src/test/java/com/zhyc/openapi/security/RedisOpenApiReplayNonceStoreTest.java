/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Redis Open API nonce 存储测试。
 */
class RedisOpenApiReplayNonceStoreTest {

  /** 固定测试时钟。 */
  private final Clock clock = Clock.fixed(Instant.parse("2026-06-24T00:00:00Z"), ZoneOffset.UTC);

  /**
   * 验证 Redis nonce 存储使用 NX + TTL 记录首次 nonce。
   */
  @Test
  void shouldRecordNonceWithNxAndTtl() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    ValueOperations<String, String> valueOperations = mockValueOperations();
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);
    RedisOpenApiReplayNonceStore store = new RedisOpenApiReplayNonceStore(redisTemplate, clock, "zhyc-test");

    assertTrue(store.recordIfAbsent("app_a", "nonce_a", clock.instant().plusSeconds(60)));
  }

  /**
   * 验证 nonce 已过期时不写入 Redis。
   */
  @Test
  void shouldRejectExpiredNonce() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    RedisOpenApiReplayNonceStore store = new RedisOpenApiReplayNonceStore(redisTemplate, clock, "zhyc-test");

    assertFalse(store.recordIfAbsent("app_a", "nonce_a", clock.instant().minusSeconds(1)));
  }

  /**
   * 验证主 nonce 存储异常时会回退到兜底存储。
   */
  @Test
  void fallbackStoreShouldUseFallbackWhenPrimaryFails() {
    OpenApiReplayNonceStore primary = new OpenApiReplayNonceStore() {
      @Override
      public void deleteExpired(Instant now) {
        throw new IllegalStateException("redis unavailable");
      }

      @Override
      public boolean isFull(int maxEntries, Instant now) {
        throw new IllegalStateException("redis unavailable");
      }

      @Override
      public boolean recordIfAbsent(String appKey, String nonce, Instant expiresAt) {
        throw new IllegalStateException("redis unavailable");
      }
    };
    OpenApiReplayNonceStore fallback = new InMemoryOpenApiReplayNonceStore();
    FallbackOpenApiReplayNonceStore store = new FallbackOpenApiReplayNonceStore(primary, fallback);

    assertFalse(store.isFull(100, clock.instant()));
    assertTrue(store.recordIfAbsent("app_a", "nonce_a", clock.instant().plusSeconds(60)));
    assertFalse(store.recordIfAbsent("app_a", "nonce_a", clock.instant().plusSeconds(60)));
  }

  @SuppressWarnings("unchecked")
  private ValueOperations<String, String> mockValueOperations() {
    return mock(ValueOperations.class);
  }
}
