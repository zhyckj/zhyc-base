/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.time.Duration;
import java.util.Objects;

/**
 * 开放 API 运行态限流策略。
 */
public class OpenApiRateLimitPolicy {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 开发者应用编码。 */
  private final String appCode;
  /** API 业务编码。 */
  private final String apiCode;
  /** 时间窗口内允许请求次数。 */
  private final int limitCount;
  /** 限流时间窗口。 */
  private final Duration window;

  /**
   * 创建开放 API 运行态限流策略。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param apiCode API 业务编码
   * @param limitCount 时间窗口内允许请求次数
   * @param window 限流时间窗口
   */
  public OpenApiRateLimitPolicy(String tenantId, String appCode, String apiCode, int limitCount,
      Duration window) {
    this.tenantId = Objects.requireNonNull(tenantId, "租户业务编码不能为空");
    this.appCode = Objects.requireNonNull(appCode, "开发者应用编码不能为空");
    this.apiCode = Objects.requireNonNull(apiCode, "API 业务编码不能为空");
    if (limitCount <= 0) {
      throw new IllegalArgumentException("限流次数必须大于 0");
    }
    if (window == null || window.isZero() || window.isNegative()) {
      throw new IllegalArgumentException("限流时间窗口必须大于 0");
    }
    this.limitCount = limitCount;
    this.window = window;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getAppCode() {
    return appCode;
  }

  public String getApiCode() {
    return apiCode;
  }

  public int getLimitCount() {
    return limitCount;
  }

  public Duration getWindow() {
    return window;
  }
}
