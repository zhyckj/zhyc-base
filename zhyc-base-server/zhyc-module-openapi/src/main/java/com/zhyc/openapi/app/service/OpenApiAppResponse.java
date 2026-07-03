/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.service;

/**
 * 开发者应用响应对象。
 */
public class OpenApiAppResponse {

    /** 应用编码。 */
    private final String appCode;
    /** 应用名称。 */
    private final String appName;
    /** 应用负责人用户主键。 */
    private final Long ownerUserId;
    /** 鉴权方式。 */
    private final String authMode;
    /** IP 白名单 JSON。 */
    private final String ipWhitelist;
    /** 应用状态。 */
    private final String status;

    /**
     * 创建开发者应用响应对象。
     *
     * @param appCode 应用编码
     * @param appName 应用名称
     * @param ownerUserId 应用负责人用户主键
     * @param authMode 鉴权方式
     * @param ipWhitelist IP 白名单 JSON
     * @param status 应用状态
     */
    public OpenApiAppResponse(String appCode, String appName, Long ownerUserId,
                              String authMode, String ipWhitelist, String status) {
        this.appCode = appCode;
        this.appName = appName;
        this.ownerUserId = ownerUserId;
        this.authMode = authMode;
        this.ipWhitelist = ipWhitelist;
        this.status = status;
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
