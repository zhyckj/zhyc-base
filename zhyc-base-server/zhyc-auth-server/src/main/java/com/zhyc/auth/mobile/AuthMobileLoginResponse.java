/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.mobile;

/**
 * 移动端登录令牌响应。
 *
 * @param accessToken 访问令牌，用于移动端 Bearer Token 请求头
 * @param tokenType 令牌类型，固定为 Bearer
 * @param expiresIn 访问令牌有效期，单位秒
 * @param tenantId 当前平台租户编码
 * @param userId 当前平台用户主键
 * @param accountName 当前平台登录账号
 */
public record AuthMobileLoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String tenantId,
    long userId,
    String accountName) {
}
