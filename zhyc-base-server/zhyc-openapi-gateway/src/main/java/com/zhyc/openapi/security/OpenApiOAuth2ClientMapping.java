/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * 开放平台 OAuth2 客户端运行态映射。
 */
public class OpenApiOAuth2ClientMapping {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 开发者应用编码。 */
  private final String appCode;
  /** 认证中心 OAuth2 客户端 ID。 */
  private final String clientId;
  /** 允许的 OAuth2 授权范围。 */
  private final String allowedScopes;

  /**
   * 创建开放平台 OAuth2 客户端运行态映射。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param clientId 认证中心 OAuth2 客户端 ID
   * @param allowedScopes 允许的 OAuth2 授权范围
   */
  public OpenApiOAuth2ClientMapping(String tenantId, String appCode, String clientId,
      String allowedScopes) {
    this.tenantId = tenantId;
    this.appCode = appCode;
    this.clientId = clientId;
    this.allowedScopes = allowedScopes;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getAppCode() {
    return appCode;
  }

  public String getClientId() {
    return clientId;
  }

  public String getAllowedScopes() {
    return allowedScopes;
  }
}
