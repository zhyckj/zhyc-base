/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.controller;

/**
 * 系统租户套餐状态变更请求。
 */
public class SysTenantPackageStatusRequest {

    /** 目标套餐状态。 */
    private String status;

    /**
     * 返回目标套餐状态。
     *
     * @return 目标套餐状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置目标套餐状态。
     *
     * @param status 目标套餐状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
