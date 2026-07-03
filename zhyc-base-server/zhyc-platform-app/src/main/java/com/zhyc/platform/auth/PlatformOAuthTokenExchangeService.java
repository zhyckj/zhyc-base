/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.auth;

/**
 * 平台 OAuth2 授权码换令牌服务。
 */
public interface PlatformOAuthTokenExchangeService {

  /**
   * 使用服务端 OAuth2 客户端凭证交换授权码。
   *
   * @param command 授权码换令牌命令
   * @return 平台 OAuth2 令牌响应
   */
  PlatformOAuthTokenResponse exchangeAuthorizationCode(PlatformOAuthTokenExchangeCommand command);

  /**
   * 使用服务端 OAuth2 客户端凭证刷新访问令牌。
   *
   * @param command 刷新令牌命令
   * @return 平台 OAuth2 令牌响应
   */
  PlatformOAuthTokenResponse refreshAccessToken(PlatformOAuthRefreshTokenCommand command);
}
