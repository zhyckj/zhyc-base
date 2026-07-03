/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * 未配置认证中心 introspection 时使用的 OAuth2 Token 校验器。
 */
public class DisabledOAuth2TokenVerifier implements OAuth2TokenVerifier {

  /** OAuth2 Token 校验未配置错误编码。 */
  public static final String ERROR_OAUTH2_NOT_CONFIGURED = "OAUTH2_NOT_CONFIGURED";

  /**
   * 拒绝 OAuth2 Token 校验。
   *
   * @param accessToken OAuth2 访问令牌
   * @return 未配置认证中心的失败结果
   */
  @Override
  public OAuth2TokenAuthenticationResult verify(String accessToken) {
    return OAuth2TokenAuthenticationResult.failure(ERROR_OAUTH2_NOT_CONFIGURED);
  }
}
