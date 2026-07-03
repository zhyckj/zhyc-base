/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.controller;

/**
 * 系统租户状态变更请求。
 */
public class SysTenantStatusRequest {

    /** 目标租户状态。 */
    private String status;

    /**
     * 返回目标租户状态。
     *
     * @return 目标租户状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置目标租户状态。
     *
     * @param status 目标租户状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
