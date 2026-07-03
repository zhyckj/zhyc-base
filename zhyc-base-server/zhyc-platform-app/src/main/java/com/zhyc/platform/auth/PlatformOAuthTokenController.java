/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.auth;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台统一认证令牌控制器。
 *
 * <p>该控制器作为后台 SPA 的 BFF 入口，负责用服务端客户端密钥把授权码交换为令牌。</p>
 */
@RestController
@RequestMapping("/auth/oauth2")
public class PlatformOAuthTokenController {

  /** 平台 OAuth2 授权码换令牌服务。 */
  private final PlatformOAuthTokenExchangeService tokenExchangeService;

  /**
   * 创建后台统一认证令牌控制器。
   *
   * @param tokenExchangeService 平台 OAuth2 授权码换令牌服务
   */
  public PlatformOAuthTokenController(PlatformOAuthTokenExchangeService tokenExchangeService) {
    this.tokenExchangeService = tokenExchangeService;
  }

  /**
   * 使用授权码交换访问令牌。
   *
   * @param request 授权码换令牌请求
   * @return 平台 OAuth2 令牌响应
   */
  @PostMapping("/token")
  public ApiResult<PlatformOAuthTokenResponse> exchangeToken(@RequestBody PlatformOAuthTokenExchangeRequest request) {
    if (request == null) {
      throw new BusinessException("AUTH_CENTER_TOKEN_REQUEST_EMPTY", "授权码换令牌请求不能为空");
    }
    return ApiResult.ok(tokenExchangeService.exchangeAuthorizationCode(request.toCommand()));
  }

  /**
   * 使用刷新令牌刷新访问令牌。
   *
   * @param request 刷新令牌请求
   * @return 平台 OAuth2 令牌响应
   */
  @PostMapping("/refresh")
  public ApiResult<PlatformOAuthTokenResponse> refreshToken(@RequestBody PlatformOAuthRefreshTokenRequest request) {
    if (request == null) {
      throw new BusinessException("AUTH_CENTER_REFRESH_REQUEST_EMPTY", "刷新令牌请求不能为空");
    }
    return ApiResult.ok(tokenExchangeService.refreshAccessToken(request.toCommand()));
  }
}
