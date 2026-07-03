/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.domain;

import java.time.LocalDateTime;

/**
 * 开发者应用领域对象。
 */
public class OpenApiApp {

    /** 主键。 */
    private Long id;
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
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建开发者应用领域对象。
     *
     * @param id 主键
     * @param tenantId 租户业务编码
     * @param appCode 应用编码
     * @param appName 应用名称
     * @param ownerUserId 应用负责人用户主键
     * @param authMode 鉴权方式
     * @param ipWhitelist IP 白名单 JSON
     * @param status 应用状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public OpenApiApp(Long id, String tenantId, String appCode, String appName, Long ownerUserId,
                      String authMode, String ipWhitelist, String status,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.appName = appName;
        this.ownerUserId = ownerUserId;
        this.authMode = authMode;
        this.ipWhitelist = ipWhitelist;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
