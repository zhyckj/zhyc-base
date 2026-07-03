/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Map;

/**
 * OAuth2 Token introspection 响应。
 */
public class OAuth2TokenIntrospectionResponse {

  /** Token 是否处于激活状态。 */
  private final boolean active;
  /** Token 声明集合。 */
  private final Map<String, Object> claims;

  private OAuth2TokenIntrospectionResponse(boolean active, Map<String, Object> claims) {
    this.active = active;
    this.claims = claims;
  }

  /**
   * 创建激活状态响应。
   *
   * @param claims Token 声明集合
   * @return 激活状态响应
   */
  public static OAuth2TokenIntrospectionResponse active(Map<String, Object> claims) {
    return new OAuth2TokenIntrospectionResponse(true, Map.copyOf(claims));
  }

  /**
   * 创建未激活状态响应。
   *
   * @return 未激活状态响应
   */
  public static OAuth2TokenIntrospectionResponse inactive() {
    return new OAuth2TokenIntrospectionResponse(false, Map.of());
  }

  public boolean isActive() {
    return active;
  }

  public Map<String, Object> getClaims() {
    return claims;
  }
}
