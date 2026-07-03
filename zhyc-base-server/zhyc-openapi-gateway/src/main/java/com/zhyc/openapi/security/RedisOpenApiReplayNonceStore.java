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
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于 Redis 的 Open API 防重放 nonce 存储。
 *
 * <p>通过 {@code SET key value NX EX} 原子写入 nonce，依赖 Redis TTL 自动清理过期记录。</p>
 */
public class RedisOpenApiReplayNonceStore implements OpenApiReplayNonceStore {

  /** Redis 字符串操作模板。 */
  private final StringRedisTemplate redisTemplate;
  /** 运行期时钟。 */
  private final Clock clock;
  /** Redis Key 前缀。 */
  private final String keyPrefix;

  /**
   * 创建 Redis nonce 存储。
   *
   * @param redisTemplate Redis 字符串操作模板
   * @param clock 运行期时钟
   * @param keyPrefix Redis Key 前缀
   */
  public RedisOpenApiReplayNonceStore(StringRedisTemplate redisTemplate, Clock clock, String keyPrefix) {
    this.redisTemplate = Objects.requireNonNull(redisTemplate, "Redis 字符串操作模板不能为空");
    this.clock = Objects.requireNonNull(clock, "运行期时钟不能为空");
    this.keyPrefix = normalizePrefix(keyPrefix);
  }

  /**
   * Redis nonce 依赖 TTL 自动过期，不需要主动清理。
   *
   * @param now 当前时间
   */
  @Override
  public void deleteExpired(Instant now) {
    // Redis nonce 通过 EX 自动过期，主动清理会引入不必要的 scan 成本。
  }

  /**
   * Redis nonce 容量由 Redis maxmemory 和淘汰策略控制。
   *
   * @param maxEntries 最大记录数
   * @param now 当前时间
   * @return 固定返回 {@code false}
   */
  @Override
  public boolean isFull(int maxEntries, Instant now) {
    return false;
  }

  /**
   * 首次写入 nonce。
   *
   * @param appKey Open API 应用标识
   * @param nonce 客户端提交的一次性随机串
   * @param expiresAt 过期时间
   * @return 写入成功返回 {@code true}
   */
  @Override
  public boolean recordIfAbsent(String appKey, String nonce, Instant expiresAt) {
    Duration ttl = Duration.between(clock.instant(), expiresAt);
    if (ttl.isZero() || ttl.isNegative()) {
      return false;
    }
    return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(buildNonceKey(appKey, nonce), "1", ttl));
  }

  private String buildNonceKey(String appKey, String nonce) {
    return keyPrefix + ":openapi:nonce:" + segment(appKey) + ':' + segment(nonce);
  }

  private String segment(String value) {
    String normalized = value == null ? "" : value;
    return normalized.length() + ":" + normalized;
  }

  private String normalizePrefix(String prefix) {
    if (prefix == null || prefix.isBlank()) {
      return "zhyc";
    }
    return prefix.trim();
  }
}
