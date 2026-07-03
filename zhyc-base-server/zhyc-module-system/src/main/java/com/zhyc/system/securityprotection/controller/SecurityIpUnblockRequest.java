/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.controller;

/**
 * 安全防护 IP 解封请求。
 */
public class SecurityIpUnblockRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** IP、IPv6 或 CIDR 规则。 */
    private String ipValue;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getIpValue() {
        return ipValue;
    }

    public void setIpValue(String ipValue) {
        this.ipValue = ipValue;
    }
}
