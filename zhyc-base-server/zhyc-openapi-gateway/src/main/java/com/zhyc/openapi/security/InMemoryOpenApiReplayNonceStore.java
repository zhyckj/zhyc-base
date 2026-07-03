/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于本地内存的 Open API 防重放 nonce 存储。
 *
 * <p>仅用于单节点本地开发或测试；集群部署应使用 JDBC、Redis 等集中式实现。</p>
 */
public class InMemoryOpenApiReplayNonceStore implements OpenApiReplayNonceStore {

  /** appKey + nonce 与过期时间的本地内存映射。 */
  private final ConcurrentMap<String, Instant> nonceExpirations = new ConcurrentHashMap<>();

  /**
   * 删除本地内存中过期的 nonce。
   *
   * @param now 当前时间
   */
  @Override
  public void deleteExpired(Instant now) {
    Objects.requireNonNull(now, "当前时间不能为空");
    nonceExpirations.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
  }

  /**
   * 判断本地 nonce 存储是否达到容量上限。
   *
   * @param maxEntries 最大 nonce 数量
   * @param now 当前时间
   * @return 达到容量上限时返回 {@code true}
   */
  @Override
  public boolean isFull(int maxEntries, Instant now) {
    deleteExpired(now);
    return nonceExpirations.size() >= maxEntries;
  }

  /**
   * 记录尚未出现过的 appKey 与 nonce 组合。
   *
   * @param appKey Open API 应用标识
   * @param nonce 客户端提交的一次性随机串
   * @param expiresAt nonce 过期时间
   * @return 首次记录成功返回 {@code true}
   */
  @Override
  public boolean recordIfAbsent(String appKey, String nonce, Instant expiresAt) {
    String key = buildNonceKey(appKey, nonce);
    return nonceExpirations.putIfAbsent(key, expiresAt) == null;
  }

  /**
   * 构造本地 nonce 存储 key，包含 appKey 维度以避免不同应用互相影响。
   *
   * @param appKey Open API 应用标识
   * @param nonce 客户端提交的一次性随机串
   * @return 本地内存存储 key
   */
  private String buildNonceKey(String appKey, String nonce) {
    return appKey.length() + ":" + appKey + ":" + nonce.length() + ":" + nonce;
  }
}
