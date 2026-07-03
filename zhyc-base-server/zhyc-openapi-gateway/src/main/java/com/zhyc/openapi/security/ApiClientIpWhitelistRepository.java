/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * 开放 API 应用客户端 IP 白名单仓储。
 *
 * <p>网关运行态通过该仓储判断已认证应用是否允许指定客户端来源 IP 访问。</p>
 */
public interface ApiClientIpWhitelistRepository {

  /**
   * 判断客户端 IP 是否允许访问指定开发者应用。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开发者应用编码
   * @param clientIp 客户端 IP
   * @return 允许访问返回 {@code true}
   */
  boolean isAllowed(String tenantId, String appCode, String clientIp);
}
