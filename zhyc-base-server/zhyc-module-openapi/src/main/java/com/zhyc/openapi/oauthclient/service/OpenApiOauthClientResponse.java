/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.service;

/**
 * 开放平台 OAuth2 客户端映射响应。
 */
public class OpenApiOauthClientResponse {

    /** 认证中心 OAuth2 客户端 ID。 */
    private final String clientId;
    /** 允许的 OAuth2 授权范围。 */
    private final String allowedScopes;
    /** 客户端映射状态。 */
    private final String status;

    /**
     * 创建开放平台 OAuth2 客户端映射响应。
     *
     * @param clientId 认证中心 OAuth2 客户端 ID
     * @param allowedScopes 允许的 OAuth2 授权范围
     * @param status 客户端映射状态
     */
    public OpenApiOauthClientResponse(String clientId, String allowedScopes, String status) {
        this.clientId = clientId;
        this.allowedScopes = allowedScopes;
        this.status = status;
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
