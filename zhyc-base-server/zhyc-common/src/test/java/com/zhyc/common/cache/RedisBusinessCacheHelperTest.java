/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.cache;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Redis 业务缓存帮助类测试。
 */
class RedisBusinessCacheHelperTest {

  /** Redis 操作入口。 */
  private RedisOperations<String, Object> redisOperations;
  /** Redis 值操作入口。 */
  private ValueOperations<String, Object> valueOperations;
  /** 被测业务缓存帮助类。 */
  private RedisBusinessCacheHelper cacheHelper;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    redisOperations = mock(RedisOperations.class);
    valueOperations = mock(ValueOperations.class);
    when(redisOperations.opsForValue()).thenReturn(valueOperations);
    cacheHelper = new RedisBusinessCacheHelper(redisOperations, "zhyc");
  }

  @Test
  void getOrLoadShouldReturnCachedValueWithoutCallingLoader() {
    when(valueOperations.get("zhyc:biz:order:detail:1001")).thenReturn("cached");
    AtomicInteger loadCount = new AtomicInteger();

    String result = cacheHelper.getOrLoad("order:detail", "1001", String.class, Duration.ofMinutes(5), () -> {
      loadCount.incrementAndGet();
      return "loaded";
    });

    assertEquals("cached", result);
    assertEquals(0, loadCount.get());
    verify(valueOperations, never()).set(any(), any(), any(Duration.class));
  }

  @Test
  void getOrLoadShouldLoadAndStoreWhenCacheMiss() {
    when(valueOperations.get("zhyc:biz:order:detail:1001")).thenReturn(null);

    String result = cacheHelper.getOrLoad("order:detail", "1001", String.class, Duration.ofMinutes(5),
        () -> "loaded");

    assertEquals("loaded", result);
    verify(valueOperations).set("zhyc:biz:order:detail:1001", "loaded", Duration.ofMinutes(5));
  }

  @Test
  void getOrLoadOptionalShouldNotStoreEmptyValue() {
    when(valueOperations.get("zhyc:biz:order:detail:1001")).thenReturn(null);

    Optional<String> result = cacheHelper.getOrLoadOptional("order:detail", "1001", String.class,
        Duration.ofMinutes(5), Optional::empty);

    assertTrue(result.isEmpty());
    verify(valueOperations, never()).set(any(), any(), any(Duration.class));
  }

  @Test
  void refreshShouldReloadAndReplaceCacheValue() {
    String result = cacheHelper.refresh("order:detail", "1001", String.class, Duration.ofMinutes(5),
        () -> "reloaded");

    assertEquals("reloaded", result);
    verify(valueOperations).set("zhyc:biz:order:detail:1001", "reloaded", Duration.ofMinutes(5));
  }

  @Test
  void multiGetShouldReturnOnlyTypeMatchedValues() {
    List<String> keys = List.of("1001", "1002", "1003");
    List<String> redisKeys = List.of("zhyc:biz:order:detail:1001", "zhyc:biz:order:detail:1002",
        "zhyc:biz:order:detail:1003");
    when(valueOperations.multiGet(redisKeys)).thenReturn(List.of("cached-1001", 1002L, "cached-1003"));

    Map<String, String> result = cacheHelper.multiGet("order:detail", keys, String.class);

    assertEquals(2, result.size());
    assertEquals("cached-1001", result.get("1001"));
    assertEquals("cached-1003", result.get("1003"));
  }

  @Test
  void setAllShouldWriteEachValueWithSameTtlAndDeleteNullValue() {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("1001", "cached-1001");
    values.put("1002", null);
    when(redisOperations.delete("zhyc:biz:order:detail:1002")).thenReturn(true);

    long result = cacheHelper.setAll("order:detail", values, Duration.ofMinutes(5));

    assertEquals(2L, result);
    verify(valueOperations).set("zhyc:biz:order:detail:1001", "cached-1001", Duration.ofMinutes(5));
    verify(redisOperations).delete("zhyc:biz:order:detail:1002");
  }

  @Test
  void getShouldReturnEmptyWhenCachedValueTypeMismatch() {
    when(valueOperations.get("zhyc:biz:order:detail:1001")).thenReturn(1001L);

    Optional<String> result = cacheHelper.get("order:detail", "1001", String.class);

    assertTrue(result.isEmpty());
  }

  @Test
  void getOrLoadShouldStillLoadWhenRedisReadFails() {
    when(valueOperations.get("zhyc:biz:order:detail:1001")).thenThrow(new IllegalStateException("redis down"));

    String result = cacheHelper.getOrLoad("order:detail", "1001", String.class, Duration.ofMinutes(5),
        () -> "loaded");

    assertEquals("loaded", result);
    verify(valueOperations).set("zhyc:biz:order:detail:1001", "loaded", Duration.ofMinutes(5));
  }

  @Test
  void setShouldReturnFalseWhenRedisWriteFails() {
    org.mockito.Mockito.doThrow(new IllegalStateException("redis down"))
        .when(valueOperations).set("zhyc:biz:order:detail:1001", "value", Duration.ofSeconds(30));

    boolean result = cacheHelper.set("order:detail", "1001", "value", Duration.ofSeconds(30));

    assertFalse(result);
  }

  @Test
  void deleteShouldDelegateToRedisKeyDelete() {
    when(redisOperations.delete("zhyc:biz:order:detail:1001")).thenReturn(true);

    boolean result = cacheHelper.delete("order:detail", "1001");

    assertTrue(result);
    verify(redisOperations).delete("zhyc:biz:order:detail:1001");
  }

  @Test
  void buildKeyShouldRejectBlankCacheNameOrKey() {
    assertThrows(IllegalArgumentException.class, () -> cacheHelper.buildKey(" ", "1001"));
    assertThrows(IllegalArgumentException.class, () -> cacheHelper.buildKey("order:detail", " "));
    assertDoesNotThrow(() -> cacheHelper.buildKey("order:detail", "1001"));
  }

  @Test
  void buildTenantKeyShouldRejectBlankTenantOrBusinessKey() {
    assertEquals("tenant-a:order:1001", cacheHelper.buildTenantKey(" tenant-a ", " order:1001 "));
    assertThrows(IllegalArgumentException.class, () -> cacheHelper.buildTenantKey(" ", "order:1001"));
    assertThrows(IllegalArgumentException.class, () -> cacheHelper.buildTenantKey("tenant-a", " "));
  }

  @Test
  void setShouldRejectInvalidTtl() {
    assertThrows(IllegalArgumentException.class,
        () -> cacheHelper.set("order:detail", "1001", "value", Duration.ZERO));
  }
}
