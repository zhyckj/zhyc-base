/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * 开放 API 限流器。
 */
public interface OpenApiRateLimiter {

  /**
   * 尝试占用一次限流配额。
   *
   * @param policy 限流策略
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param apiCode API 业务编码
   * @return 限流判定结果
   */
  OpenApiRateLimitResult tryAcquire(OpenApiRateLimitPolicy policy, String tenantId,
      String appCode, String apiCode);
}
