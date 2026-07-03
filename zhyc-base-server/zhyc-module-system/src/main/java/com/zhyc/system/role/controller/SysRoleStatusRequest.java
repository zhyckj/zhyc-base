/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.controller;

/**
 * 系统角色状态调整请求。
 */
public class SysRoleStatusRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 角色状态，enabled 或 disabled。 */
    private String status;

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 返回角色状态。
     *
     * @return 角色状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置角色状态。
     *
     * @param status 角色状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
