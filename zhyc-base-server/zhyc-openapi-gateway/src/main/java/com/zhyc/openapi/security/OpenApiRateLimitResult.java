/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * 开放 API 限流判定结果。
 */
public class OpenApiRateLimitResult {

  /** 是否允许请求继续处理。 */
  private final boolean allowed;
  /** 失败原因编码。 */
  private final String failureCode;
  /** 客户端再次尝试前应等待的秒数；仅限流拒绝时有值。 */
  private final long retryAfterSeconds;

  private OpenApiRateLimitResult(boolean allowed, String failureCode, long retryAfterSeconds) {
    this.allowed = allowed;
    this.failureCode = failureCode;
    this.retryAfterSeconds = retryAfterSeconds;
  }

  /**
   * 创建允许结果。
   *
   * @return 允许结果
   */
  public static OpenApiRateLimitResult allowed() {
    return new OpenApiRateLimitResult(true, null, 0);
  }

  /**
   * 创建拒绝结果。
   *
   * @param failureCode 失败原因编码
   * @return 拒绝结果
   */
  public static OpenApiRateLimitResult rejected(String failureCode) {
    return rejected(failureCode, 0);
  }

  /**
   * 创建带重试等待时间的拒绝结果。
   *
   * @param failureCode 失败原因编码
   * @param retryAfterSeconds 客户端再次尝试前应等待的秒数
   * @return 拒绝结果
   */
  public static OpenApiRateLimitResult rejected(String failureCode, long retryAfterSeconds) {
    return new OpenApiRateLimitResult(false, failureCode, Math.max(0, retryAfterSeconds));
  }

  public boolean isAllowed() {
    return allowed;
  }

  public String getFailureCode() {
    return failureCode;
  }

  public long getRetryAfterSeconds() {
    return retryAfterSeconds;
  }
}
