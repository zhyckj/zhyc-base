/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.controller;

/**
 * 系统用户重置密码请求。
 */
public class SysUserResetPasswordRequest {

    /** 新密码明文，仅用于本次请求，后端会立即转换为 Shiro 哈希。 */
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
