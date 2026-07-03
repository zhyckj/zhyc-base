/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.domain;

import java.time.LocalDateTime;

/**
 * 开放平台 OAuth2 客户端映射领域对象。
 */
public class OpenApiOauthClient {

    /** 主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 开发者应用编码。 */
    private String appCode;
    /** 认证中心 OAuth2 客户端 ID。 */
    private String clientId;
    /** 允许的 OAuth2 授权范围，多个 scope 使用空格分隔。 */
    private String allowedScopes;
    /** 客户端映射状态。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建开放平台 OAuth2 客户端映射领域对象。
     *
     * @param id 主键
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @param clientId 认证中心 OAuth2 客户端 ID
     * @param allowedScopes 允许的 OAuth2 授权范围
     * @param status 客户端映射状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public OpenApiOauthClient(Long id, String tenantId, String appCode, String clientId,
                              String allowedScopes, String status,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.clientId = clientId;
        this.allowedScopes = allowedScopes;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
