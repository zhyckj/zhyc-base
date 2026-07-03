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
 * 开放 API 运行态签名策略。
 *
 * <p>该对象由网关读取管理端维护的签名策略表后使用，控制 API Key 签名算法、时间戳窗口和 nonce 有效期。</p>
 */
public class OpenApiSignaturePolicy {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 开发者应用编码。 */
  private final String appCode;
  /** 签名算法，首期支持 HMAC_SHA256。 */
  private final String algorithm;
  /** 客户端时间戳允许偏差窗口。 */
  private final Duration timestampTolerance;
  /** nonce 防重放有效期。 */
  private final Duration nonceTtl;
  /** 是否要求请求体参与摘要。 */
  private final boolean requireBodyHash;

  /**
   * 创建开放 API 运行态签名策略。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param algorithm 签名算法
   * @param timestampTolerance 客户端时间戳允许偏差窗口
   * @param nonceTtl nonce 防重放有效期
   * @param requireBodyHash 是否要求请求体参与摘要
   */
  public OpenApiSignaturePolicy(String tenantId, String appCode, String algorithm,
      Duration timestampTolerance, Duration nonceTtl, boolean requireBodyHash) {
    this.tenantId = requireText(tenantId, "租户业务编码不能为空");
    this.appCode = requireText(appCode, "开发者应用编码不能为空");
    this.algorithm = requireText(algorithm, "签名算法不能为空");
    this.timestampTolerance = requirePositiveDuration(timestampTolerance, "时间戳窗口必须大于 0");
    this.nonceTtl = requirePositiveDuration(nonceTtl, "nonce 有效期必须大于 0");
    this.requireBodyHash = requireBodyHash;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getAppCode() {
    return appCode;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public Duration getTimestampTolerance() {
    return timestampTolerance;
  }

  public Duration getNonceTtl() {
    return nonceTtl;
  }

  public boolean isRequireBodyHash() {
    return requireBodyHash;
  }

  private String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private Duration requirePositiveDuration(Duration value, String message) {
    Objects.requireNonNull(value, message);
    if (value.isZero() || value.isNegative()) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }
}
