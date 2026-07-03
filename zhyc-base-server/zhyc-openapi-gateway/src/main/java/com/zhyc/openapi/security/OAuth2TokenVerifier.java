/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * OAuth2 Token 校验器。
 */
public interface OAuth2TokenVerifier {

  /**
   * 校验 OAuth2 访问令牌。
   *
   * @param accessToken OAuth2 访问令牌
   * @return Token 鉴权结果
   */
  OAuth2TokenAuthenticationResult verify(String accessToken);
}
