/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.time.Instant;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 带兜底能力的 Open API nonce 存储。
 *
 * <p>优先使用 Redis 记录 nonce，Redis 异常时回退 JDBC 存储，保障防重放能力在缓存故障时仍可用。</p>
 */
public class FallbackOpenApiReplayNonceStore implements OpenApiReplayNonceStore {

  /** nonce 存储兜底日志。 */
  private static final Logger LOGGER = LoggerFactory.getLogger(FallbackOpenApiReplayNonceStore.class);

  /** 主 nonce 存储。 */
  private final OpenApiReplayNonceStore primary;
  /** 兜底 nonce 存储。 */
  private final OpenApiReplayNonceStore fallback;

  /**
   * 创建带兜底能力的 nonce 存储。
   *
   * @param primary 主 nonce 存储
   * @param fallback 兜底 nonce 存储
   */
  public FallbackOpenApiReplayNonceStore(OpenApiReplayNonceStore primary, OpenApiReplayNonceStore fallback) {
    this.primary = Objects.requireNonNull(primary, "主 nonce 存储不能为空");
    this.fallback = Objects.requireNonNull(fallback, "兜底 nonce 存储不能为空");
  }

  @Override
  public void deleteExpired(Instant now) {
    try {
      primary.deleteExpired(now);
    } catch (RuntimeException ex) {
      LOGGER.warn("Redis nonce 过期清理失败，已回退 JDBC 清理", ex);
      fallback.deleteExpired(now);
    }
  }

  @Override
  public boolean isFull(int maxEntries, Instant now) {
    try {
      return primary.isFull(maxEntries, now);
    } catch (RuntimeException ex) {
      LOGGER.warn("Redis nonce 容量检查失败，已回退 JDBC 检查", ex);
      return fallback.isFull(maxEntries, now);
    }
  }

  @Override
  public boolean recordIfAbsent(String appKey, String nonce, Instant expiresAt) {
    try {
      return primary.recordIfAbsent(appKey, nonce, expiresAt);
    } catch (RuntimeException ex) {
      LOGGER.warn("Redis nonce 写入失败，已回退 JDBC 写入，appKey={}", appKey, ex);
      return fallback.recordIfAbsent(appKey, nonce, expiresAt);
    }
  }
}
