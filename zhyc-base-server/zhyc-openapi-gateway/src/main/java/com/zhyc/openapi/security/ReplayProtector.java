/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Open API 请求防重放保护器。
 *
 * <p>防重放逻辑负责校验时间窗口和参数合法性，nonce 存储由 {@link OpenApiReplayNonceStore}
 * 扩展点承载。默认构造器使用本地内存存储，网关 Spring 配置默认注入 JDBC 存储以支持集群部署。</p>
 */
public class ReplayProtector {

  /** 默认允许的请求时间窗口。 */
  private static final Duration DEFAULT_REPLAY_WINDOW = Duration.ofMinutes(5);

  /** 默认最大本地 nonce 记录数量。 */
  private static final int DEFAULT_MAX_ENTRIES = 100_000;

  /** 旧接口未提供 appKey 时使用的默认应用维度。 */
  private static final String DEFAULT_APP_KEY = "_default";

  /** 请求可接受的最大时间偏移窗口。 */
  private final Duration replayWindow;

  /** 当前时间来源，便于测试或后续替换。 */
  private final Clock clock;

  /** 本地内存最多保存的 nonce 记录数。 */
  private final int maxEntries;
  /** nonce 存储实现。 */
  private final OpenApiReplayNonceStore nonceStore;

  /**
   * 使用默认 5 分钟窗口创建防重放保护器。
   */
  public ReplayProtector() {
    this(DEFAULT_REPLAY_WINDOW, Clock.systemUTC(), DEFAULT_MAX_ENTRIES);
  }

  /**
   * 使用指定时间窗口创建防重放保护器。
   *
   * @param replayWindow nonce 有效窗口
   */
  public ReplayProtector(Duration replayWindow) {
    this(replayWindow, Clock.systemUTC(), DEFAULT_MAX_ENTRIES);
  }

  /**
   * 使用指定时间窗口和时钟创建防重放保护器，并采用默认最大容量。
   *
   * @param replayWindow nonce 有效窗口
   * @param clock 当前时间来源
   */
  public ReplayProtector(Duration replayWindow, Clock clock) {
    this(replayWindow, clock, DEFAULT_MAX_ENTRIES);
  }

  /**
   * 使用指定时间窗口、时钟和最大容量创建防重放保护器。
   *
   * @param replayWindow nonce 有效窗口
   * @param clock 当前时间来源
   * @param maxEntries 本地内存最多保存的 nonce 记录数
   */
  public ReplayProtector(Duration replayWindow, Clock clock, int maxEntries) {
    this(replayWindow, clock, maxEntries, new InMemoryOpenApiReplayNonceStore());
  }

  /**
   * 使用指定时间窗口、时钟、最大容量和 nonce 存储创建防重放保护器。
   *
   * @param replayWindow nonce 有效窗口
   * @param clock 当前时间来源
   * @param maxEntries 最多保存的 nonce 记录数
   * @param nonceStore nonce 存储实现
   */
  public ReplayProtector(Duration replayWindow, Clock clock, int maxEntries,
      OpenApiReplayNonceStore nonceStore) {
    this.replayWindow = Objects.requireNonNull(replayWindow, "replayWindow must not be null");
    this.clock = Objects.requireNonNull(clock, "clock must not be null");
    this.nonceStore = Objects.requireNonNull(nonceStore, "nonce 存储实现不能为空");
    if (replayWindow.isZero() || replayWindow.isNegative()) {
      throw new IllegalArgumentException("replayWindow must be positive");
    }
    if (maxEntries <= 0) {
      throw new IllegalArgumentException("maxEntries must be positive");
    }
    this.maxEntries = maxEntries;
  }

  /**
   * 使用默认应用维度校验 nonce 是否未被使用且请求时间仍在允许窗口内。
   *
   * @param nonce 客户端提交的一次性随机串
   * @param requestTime 客户端请求时间
   * @return 首次使用且未过期返回 {@code true}，重复 nonce 或超出窗口返回 {@code false}
   */
  public boolean checkAndRecord(String nonce, Instant requestTime) {
    return accept(DEFAULT_APP_KEY, nonce, requestTime);
  }

  /**
   * 按应用维度校验 nonce 是否未被使用且请求时间仍在允许窗口内，校验通过后会记录该 nonce。
   *
   * <p>nonce 有效期保存到 {@code max(now, requestTime) + replayWindow}，确保允许窗口内的未来
   * 时间戳不会在本地记录提前过期后被二次接受。</p>
   *
   * @param appKey Open API 应用标识，不允许为空白
   * @param nonce 客户端提交的一次性随机串，不允许为空白
   * @param requestTime 客户端请求时间
   * @return 首次使用且未过期返回 {@code true}，重复 nonce、超出窗口或容量已满返回 {@code false}
   */
  public synchronized boolean accept(String appKey, String nonce, Instant requestTime) {
    return accept(appKey, nonce, requestTime, replayWindow, replayWindow);
  }

  /**
   * 按应用维度校验 nonce 是否未被使用，并使用指定时间戳窗口和 nonce 有效期记录该 nonce。
   *
   * @param appKey Open API 应用标识，不允许为空白
   * @param nonce 客户端提交的一次性随机串，不允许为空白
   * @param requestTime 客户端请求时间
   * @param timestampWindow 客户端时间戳允许偏差窗口
   * @param nonceTtl nonce 记录有效期
   * @return 首次使用且未过期返回 {@code true}，重复 nonce、超出窗口或容量已满返回 {@code false}
   */
  public synchronized boolean accept(String appKey, String nonce, Instant requestTime,
      Duration timestampWindow, Duration nonceTtl) {
    if (appKey == null || appKey.isBlank() || nonce == null || nonce.isBlank()
        || requestTime == null) {
      return false;
    }
    requirePositiveDuration(timestampWindow);
    requirePositiveDuration(nonceTtl);

    Instant now = Instant.now(clock);
    nonceStore.deleteExpired(now);
    if (!isWithinWindow(requestTime, timestampWindow)) {
      return false;
    }

    if (nonceStore.isFull(maxEntries, now)) {
      return false;
    }

    Instant expiresAt = laterOf(now, requestTime).plus(nonceTtl);
    return nonceStore.recordIfAbsent(appKey, nonce, expiresAt);
  }

  /**
   * 判断请求时间是否处于允许的防重放时间窗口内。
   *
   * @param requestTime 客户端请求时间
   * @return 请求时间未超出当前时间正负窗口返回 {@code true}
   */
  public boolean isWithinWindow(Instant requestTime) {
    return isWithinWindow(requestTime, replayWindow);
  }

  /**
   * 判断请求时间是否处于指定允许时间窗口内。
   *
   * @param requestTime 客户端请求时间
   * @param timestampWindow 客户端时间戳允许偏差窗口
   * @return 请求时间未超出当前时间正负窗口返回 {@code true}
   */
  public boolean isWithinWindow(Instant requestTime, Duration timestampWindow) {
    if (requestTime == null) {
      return false;
    }
    requirePositiveDuration(timestampWindow);
    Instant now = Instant.now(clock);
    return !requestTime.isBefore(now.minus(timestampWindow))
        && !requestTime.isAfter(now.plus(timestampWindow));
  }

  /**
   * 返回两个时间中更晚的时间。
   *
   * @param first 第一个时间
   * @param second 第二个时间
   * @return 更晚的时间
   */
  private Instant laterOf(Instant first, Instant second) {
    return first.isAfter(second) ? first : second;
  }

  private void requirePositiveDuration(Duration duration) {
    Objects.requireNonNull(duration, "时间窗口不能为空");
    if (duration.isZero() || duration.isNegative()) {
      throw new IllegalArgumentException("时间窗口必须大于 0");
    }
  }
}
