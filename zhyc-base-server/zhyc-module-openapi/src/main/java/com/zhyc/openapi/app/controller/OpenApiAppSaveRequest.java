/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.controller;

/**
 * 开发者应用保存请求。
 */
public class OpenApiAppSaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 应用编码。 */
    private String appCode;
    /** 应用名称。 */
    private String appName;
    /** 应用负责人用户主键。 */
    private Long ownerUserId;
    /** 鉴权方式。 */
    private String authMode;
    /** IP 白名单 JSON。 */
    private String ipWhitelist;
    /** 应用状态。 */
    private String status;

    public String getTenantId() {
        return tenantId;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getAppName() {
        return appName;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public String getAuthMode() {
        return authMode;
    }

    public String getIpWhitelist() {
        return ipWhitelist;
    }

    public String getStatus() {
        return status;
    }
}
