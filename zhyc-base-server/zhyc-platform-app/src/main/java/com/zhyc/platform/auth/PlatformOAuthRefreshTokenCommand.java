/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.auth;

/**
 * 平台 OAuth2 刷新令牌命令。
 *
 * @param refreshToken 认证中心签发的刷新令牌，只允许提交给核心平台服务端换取新访问令牌
 */
public record PlatformOAuthRefreshTokenCommand(String refreshToken) {
}
