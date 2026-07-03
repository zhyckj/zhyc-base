/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.controller;

/**
 * 系统密钥状态请求。
 */
public class SysSecretStatusRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 密钥状态，启用或停用。 */
    private String status;

    /** @return 租户业务编码 */
    public String getTenantId() {
        return tenantId;
    }

    /** @param tenantId 租户业务编码 */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /** @return 密钥状态 */
    public String getStatus() {
        return status;
    }

    /** @param status 密钥状态 */
    public void setStatus(String status) {
        this.status = status;
    }
}
