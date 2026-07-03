/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.auth;

/**
 * 平台 OAuth2 授权码换令牌命令。
 *
 * @param code 认证中心返回的授权码
 * @param redirectUri 后台管理端授权回调地址，必须与认证中心注册值一致
 * @param codeVerifier PKCE 原始校验码，必须与发起授权时生成的 code_challenge 匹配
 */
public record PlatformOAuthTokenExchangeCommand(String code, String redirectUri, String codeVerifier) {
}
