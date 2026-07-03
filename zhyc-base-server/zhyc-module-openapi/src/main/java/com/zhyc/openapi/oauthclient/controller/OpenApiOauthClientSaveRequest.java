/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.controller;

/**
 * 开放平台 OAuth2 客户端映射保存请求。
 */
public class OpenApiOauthClientSaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 开发者应用编码。 */
    private String appCode;
    /** 认证中心 OAuth2 客户端 ID。 */
    private String clientId;
    /** 允许的 OAuth2 授权范围。 */
    private String allowedScopes;
    /** 客户端映射状态。 */
    private String status;

    public String getTenantId() {
        return tenantId;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAllowedScopes() {
        return allowedScopes;
    }

    public String getStatus() {
        return status;
    }
}
