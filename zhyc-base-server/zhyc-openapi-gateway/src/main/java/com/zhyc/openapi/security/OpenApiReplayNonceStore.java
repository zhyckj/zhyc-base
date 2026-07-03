/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.time.Instant;

/**
 * Open API 防重放 nonce 存储。
 */
public interface OpenApiReplayNonceStore {

  /**
   * 删除已经过期的 nonce 记录。
   *
   * @param now 当前时间
   */
  void deleteExpired(Instant now);

  /**
   * 判断当前存储是否已经达到容量上限。
   *
   * @param maxEntries 最大记录数
   * @param now 当前时间
   * @return 已达到容量上限返回 {@code true}
   */
  boolean isFull(int maxEntries, Instant now);

  /**
   * 首次写入 nonce，已存在时返回失败。
   *
   * @param appKey Open API 应用标识
   * @param nonce 客户端提交的一次性随机串
   * @param expiresAt 过期时间
   * @return 写入成功返回 {@code true}，重复 nonce 返回 {@code false}
   */
  boolean recordIfAbsent(String appKey, String nonce, Instant expiresAt);
}
