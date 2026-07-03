/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Optional;

/**
 * 开放 API 运行态限流策略仓储。
 */
public interface OpenApiRateLimitPolicyRepository {

  /**
   * 查询启用状态的应用 API 限流策略。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param apiCode API 业务编码
   * @return 启用状态的限流策略
   */
  Optional<OpenApiRateLimitPolicy> findEnabledPolicy(String tenantId, String appCode, String apiCode);
}
