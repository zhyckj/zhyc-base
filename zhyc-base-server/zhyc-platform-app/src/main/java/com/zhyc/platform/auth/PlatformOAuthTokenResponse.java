/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.auth;

/**
 * 平台 OAuth2 令牌响应。
 *
 * <p>该对象只返回浏览器后续访问核心平台所需的令牌数据，不包含客户端密钥。</p>
 *
 * @param accessToken 访问令牌，用于后台管理端 Bearer Token 请求头
 * @param refreshToken 刷新令牌，首期返回给前端受控保存
 * @param tokenType 令牌类型，通常为 Bearer
 * @param expiresIn 访问令牌有效期，单位秒
 * @param scope 授权范围
 */
public record PlatformOAuthTokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    String scope) {
}
