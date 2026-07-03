/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * API Key 鉴权结果。
 */
public class ApiKeyAuthenticationResult {

  /** 是否鉴权成功。 */
  private final boolean authenticated;
  /** 失败原因编码。 */
  private final String failureCode;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 开发者应用编码。 */
  private final String appCode;

  private ApiKeyAuthenticationResult(boolean authenticated, String failureCode, String tenantId,
      String appCode) {
    this.authenticated = authenticated;
    this.failureCode = failureCode;
    this.tenantId = tenantId;
    this.appCode = appCode;
  }

  /**
   * 创建鉴权成功结果。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @return 鉴权成功结果
   */
  public static ApiKeyAuthenticationResult success(String tenantId, String appCode) {
    return new ApiKeyAuthenticationResult(true, null, tenantId, appCode);
  }

  /**
   * 创建鉴权失败结果。
   *
   * @param failureCode 失败原因编码
   * @return 鉴权失败结果
   */
  public static ApiKeyAuthenticationResult failure(String failureCode) {
    return new ApiKeyAuthenticationResult(false, failureCode, null, null);
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public String getFailureCode() {
    return failureCode;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getAppCode() {
    return appCode;
  }
}
