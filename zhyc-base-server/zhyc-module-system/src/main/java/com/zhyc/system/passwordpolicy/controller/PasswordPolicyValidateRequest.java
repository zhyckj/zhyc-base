/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.controller;

/**
 * 密码策略校验请求。
 */
public class PasswordPolicyValidateRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 待校验密码明文，仅用于内存校验，不得持久化或输出日志。 */
    private String password;

    public String getTenantId() {
        return tenantId;
    }

    public String getPassword() {
        return password;
    }
}
