/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Optional;

/**
 * 开放平台 OAuth2 客户端运行态映射仓储。
 */
public interface OpenApiOAuth2ClientMappingRepository {

  /**
   * 按租户和 OAuth2 客户端 ID 查询启用映射。
   *
   * @param tenantId 租户业务编码
   * @param clientId 认证中心 OAuth2 客户端 ID
   * @return 启用状态的 OAuth2 客户端映射
   */
  Optional<OpenApiOAuth2ClientMapping> findEnabledByTenantIdAndClientId(String tenantId, String clientId);
}
