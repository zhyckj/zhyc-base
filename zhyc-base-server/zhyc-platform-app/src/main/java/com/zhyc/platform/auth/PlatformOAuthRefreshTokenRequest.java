/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.auth;

/**
 * 后台 OAuth2 刷新令牌请求。
 *
 * @param refreshToken 浏览器本地保存的刷新令牌；服务端使用 client secret 调用认证中心刷新访问令牌
 */
public record PlatformOAuthRefreshTokenRequest(String refreshToken) {

  /**
   * 转换为刷新令牌命令。
   *
   * @return 平台 OAuth2 刷新令牌命令
   */
  public PlatformOAuthRefreshTokenCommand toCommand() {
    return new PlatformOAuthRefreshTokenCommand(refreshToken);
  }
}
