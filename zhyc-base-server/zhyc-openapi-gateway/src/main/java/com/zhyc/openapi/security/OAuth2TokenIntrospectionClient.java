/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * OAuth2 Token introspection 客户端。
 */
public interface OAuth2TokenIntrospectionClient {

  /**
   * 调用认证中心 introspection 端点校验访问令牌。
   *
   * @param accessToken OAuth2 访问令牌
   * @return introspection 响应
   */
  OAuth2TokenIntrospectionResponse introspect(String accessToken);
}
