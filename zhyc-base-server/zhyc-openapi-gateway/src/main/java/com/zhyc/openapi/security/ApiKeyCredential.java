/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.time.Instant;

/**
 * API Key 运行态凭证。
 *
 * <p>secretValue 表示运行时可用于 HMAC 校验的密钥材料，生产实现应由密文或密钥服务解密后注入，
 * 不得将 API Secret 明文落库或输出日志。</p>
 */
public class ApiKeyCredential {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 开发者应用编码。 */
  private final String appCode;
  /** API 访问密钥。 */
  private final String accessKey;
  /** 运行时 API Secret。 */
  private final String secretValue;
  /** 凭证状态。 */
  private final String status;
  /** 凭证过期时间。 */
  private final Instant expireAt;

  /**
   * 创建 API Key 运行态凭证。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param accessKey API 访问密钥
   * @param secretValue 运行时 API Secret
   * @param status 凭证状态
   * @param expireAt 凭证过期时间
   */
  public ApiKeyCredential(String tenantId, String appCode, String accessKey, String secretValue,
      String status, Instant expireAt) {
    this.tenantId = tenantId;
    this.appCode = appCode;
    this.accessKey = accessKey;
    this.secretValue = secretValue;
    this.status = status;
    this.expireAt = expireAt;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getAppCode() {
    return appCode;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getSecretValue() {
    return secretValue;
  }

  public String getStatus() {
    return status;
  }

  public Instant getExpireAt() {
    return expireAt;
  }

  /**
   * 复制当前凭证并替换状态。
   *
   * @param status 新状态
   * @return 替换状态后的凭证
   */
  public ApiKeyCredential withStatus(String status) {
    return new ApiKeyCredential(tenantId, appCode, accessKey, secretValue, status, expireAt);
  }

  /**
   * 复制当前凭证并替换过期时间。
   *
   * @param expireAt 新过期时间
   * @return 替换过期时间后的凭证
   */
  public ApiKeyCredential withExpireAt(Instant expireAt) {
    return new ApiKeyCredential(tenantId, appCode, accessKey, secretValue, status, expireAt);
  }

  /**
   * 判断凭证状态是否为启用。
   *
   * @return 状态为 enabled 返回 {@code true}
   */
  public boolean isEnabled() {
    try {
      return OpenApiGatewayCredentialStatus.fromCode(status).isEnabled();
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }
}
