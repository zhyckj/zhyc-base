/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * 开放 API 运行态权限仓储。
 */
public interface ApiPermissionRepository {

  /**
   * 判断应用是否被授权访问指定开放 API。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param httpMethod HTTP 方法
   * @param requestPath 请求路径
   * @return 已授权返回 {@code true}
   */
  boolean isAllowed(String tenantId, String appCode, String httpMethod, String requestPath);

  /**
   * 解析当前请求命中的开放 API 业务编码。
   *
   * <p>默认使用请求路径作为审计编码，运行期仓储应优先返回授权表中的 {@code api_code}，
   * 便于审计报表按接口目录聚合。</p>
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param httpMethod HTTP 方法
   * @param requestPath 请求路径
   * @return 命中的开放 API 业务编码
   */
  default String resolveApiCode(String tenantId, String appCode, String httpMethod, String requestPath) {
    return requestPath;
  }
}
