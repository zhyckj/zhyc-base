/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 基于 Redis 固定窗口的开放 API 限流器。
 *
 * <p>使用 Lua 脚本原子执行 {@code INCR + EXPIRE}，避免首次写入后未设置过期时间导致计数器泄漏。</p>
 */
public class RedisOpenApiRateLimiter implements OpenApiRateLimiter {

  /** 限流拒绝错误编码。 */
  public static final String ERROR_RATE_LIMITED = "RATE_LIMITED";

  /** Redis 原子计数脚本。 */
  private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>("""
      local current = redis.call('INCR', KEYS[1])
      if current == 1 then
        redis.call('EXPIRE', KEYS[1], ARGV[1])
      end
      return current
      """, Long.class);

  /** Redis 字符串操作模板。 */
  private final StringRedisTemplate redisTemplate;
  /** 运行期时钟。 */
  private final Clock clock;
  /** Redis Key 前缀。 */
  private final String keyPrefix;

  /**
   * 创建 Redis 固定窗口限流器。
   *
   * @param redisTemplate Redis 字符串操作模板
   * @param clock 运行期时钟
   * @param keyPrefix Redis Key 前缀
   */
  public RedisOpenApiRateLimiter(StringRedisTemplate redisTemplate, Clock clock, String keyPrefix) {
    this.redisTemplate = Objects.requireNonNull(redisTemplate, "Redis 字符串操作模板不能为空");
    this.clock = Objects.requireNonNull(clock, "运行期时钟不能为空");
    this.keyPrefix = normalizePrefix(keyPrefix);
  }

  /**
   * 尝试占用一次 Redis 共享限流配额。
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
    Long current = redisTemplate.execute(INCREMENT_SCRIPT, List.of(windowKey.redisKey()),
        String.valueOf(windowKey.expireSeconds()));
    long currentCount = current == null ? 0 : current;
    if (currentCount > policy.getLimitCount()) {
      Instant now = clock.instant();
      long retryAfterSeconds = Math.max(1, windowKey.windowEndSecond() - now.getEpochSecond());
      return OpenApiRateLimitResult.rejected(ERROR_RATE_LIMITED, retryAfterSeconds);
    }
    return OpenApiRateLimitResult.allowed();
  }

  private WindowKey buildWindowKey(OpenApiRateLimitPolicy policy, String tenantId, String appCode,
      String apiCode) {
    long windowSeconds = policy.getWindow().toSeconds();
    long epochSecond = clock.instant().getEpochSecond();
    long windowIndex = epochSecond / windowSeconds;
    long windowEndSecond = (windowIndex + 1) * windowSeconds;
    long expireSeconds = Math.max(1, windowEndSecond - epochSecond + 1);
    String redisKey = keyPrefix + ":openapi:ratelimit:"
        + segment(tenantId) + ':' + segment(appCode) + ':' + segment(apiCode)
        + ':' + windowSeconds + ':' + windowIndex;
    return new WindowKey(redisKey, expireSeconds, windowEndSecond);
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

  /**
   * Redis 限流窗口键。
   *
   * @param redisKey Redis 计数键
   * @param expireSeconds Redis 过期秒数
   * @param windowEndSecond 固定窗口结束时间戳秒
   */
  private record WindowKey(String redisKey, long expireSeconds, long windowEndSecond) {
  }
}
