/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.mobile;

/**
 * 移动端账号密码登录请求。
 *
 * @param username 认证中心登录账号
 * @param password 认证中心登录密码，仅用于本次认证校验，不写入日志和响应
 */
public record AuthMobileLoginRequest(String username, String password) {
}
