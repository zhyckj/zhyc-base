/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.auth;

/**
 * 后台管理端授权码换令牌请求。
 *
 * @param code 认证中心授权码
 * @param redirectUri 后台管理端回调地址
 * @param codeVerifier PKCE 原始校验码，用于认证中心校验授权码归属
 */
public record PlatformOAuthTokenExchangeRequest(String code, String redirectUri, String codeVerifier) {

  /**
   * 转换为服务层命令。
   *
   * @return 平台 OAuth2 授权码换令牌命令
   */
  public PlatformOAuthTokenExchangeCommand toCommand() {
    return new PlatformOAuthTokenExchangeCommand(code, redirectUri, codeVerifier);
  }
}
