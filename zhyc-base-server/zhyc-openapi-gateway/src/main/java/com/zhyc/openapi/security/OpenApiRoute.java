/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * 开放 API 运行态后端路由。
 */
public class OpenApiRoute {

  /** API 业务编码。 */
  private final String apiCode;
  /** 后端转发路由。 */
  private final String backendRoute;

  /**
   * 创建开放 API 运行态后端路由。
   *
   * @param apiCode API 业务编码
   * @param backendRoute 后端转发路由
   */
  public OpenApiRoute(String apiCode, String backendRoute) {
    this.apiCode = requireText(apiCode, "API 业务编码不能为空");
    this.backendRoute = requireText(backendRoute, "后端转发路由不能为空");
  }

  public String getApiCode() {
    return apiCode;
  }

  public String getBackendRoute() {
    return backendRoute;
  }

  private String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }
}
