/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/**
 * 请求防重放保护器测试，覆盖重复 nonce、过期、应用隔离和容量限制。
 */
class ReplayProtectorTest {

  @Test
  void acceptReturnsTrueFirstTimeFalseOnRepeatAndAllowsReuseAfterExpiration() {
    MutableClock clock = new MutableClock(Instant.parse("2026-06-24T00:00:00Z"));
    ReplayProtector protector = new ReplayProtector(Duration.ofSeconds(10), clock, 100);

    assertTrue(protector.accept("app-a", "nonce-001", clock.instant()));
    assertFalse(protector.accept("app-a", "nonce-001", clock.instant()));

    clock.setInstant(Instant.parse("2026-06-24T00:00:11Z"));

    assertTrue(protector.accept("app-a", "nonce-001", clock.instant()));
  }

  @Test
  void acceptKeepsFutureTimestampNonceUntilWholeAllowedWindowExpires() {
    MutableClock clock = new MutableClock(Instant.parse("2026-06-24T00:00:00Z"));
    ReplayProtector protector = new ReplayProtector(Duration.ofSeconds(10), clock, 100);
    Instant futureRequestTime = Instant.parse("2026-06-24T00:00:05Z");

    assertTrue(protector.accept("app-a", "nonce-001", futureRequestTime));

    clock.setInstant(Instant.parse("2026-06-24T00:00:11Z"));

    assertFalse(protector.accept("app-a", "nonce-001", futureRequestTime));
  }

  @Test
  void acceptScopesNonceByAppKey() {
    MutableClock clock = new MutableClock(Instant.parse("2026-06-24T00:00:00Z"));
    ReplayProtector protector = new ReplayProtector(Duration.ofSeconds(10), clock, 100);

    assertTrue(protector.accept("app-a", "same-nonce", clock.instant()));
    assertTrue(protector.accept("app-b", "same-nonce", clock.instant()));
    assertFalse(protector.accept("app-a", "same-nonce", clock.instant()));
  }

  @Test
  void acceptRejectsBlankAppKeyOrNonce() {
    MutableClock clock = new MutableClock(Instant.parse("2026-06-24T00:00:00Z"));
    ReplayProtector protector = new ReplayProtector(Duration.ofSeconds(10), clock, 100);

    assertFalse(protector.accept(" ", "nonce-001", clock.instant()));
    assertFalse(protector.accept("app-a", " ", clock.instant()));
  }

  @Test
  void acceptRejectsNewNonceWhenCapacityIsFull() {
    MutableClock clock = new MutableClock(Instant.parse("2026-06-24T00:00:00Z"));
    ReplayProtector protector = new ReplayProtector(Duration.ofSeconds(10), clock, 1);

    assertTrue(protector.accept("app-a", "nonce-001", clock.instant()));
    assertFalse(protector.accept("app-a", "nonce-002", clock.instant()));
  }

  /**
   * 测试用可变时钟，用于稳定模拟 nonce 过期和未来时间戳窗口。
   */
  private static final class MutableClock extends Clock {

    /** 当前测试时间。 */
    private Instant instant;

    private MutableClock(Instant instant) {
      this.instant = instant;
    }

    private void setInstant(Instant instant) {
      this.instant = instant;
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }
}
