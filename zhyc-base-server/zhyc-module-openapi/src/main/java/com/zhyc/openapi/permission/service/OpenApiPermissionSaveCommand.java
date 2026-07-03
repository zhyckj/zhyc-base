/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission.service;

/**
 * 开放 API 权限授权保存命令。
 */
public class OpenApiPermissionSaveCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** 开发者应用编码。 */
    private final String appCode;
    /** API 业务编码。 */
    private final String apiCode;
    /** API 名称。 */
    private final String apiName;
    /** HTTP 方法。 */
    private final String httpMethod;
    /** 请求路径匹配规则。 */
    private final String pathPattern;
    /** 授权状态。 */
    private final String status;

    /**
     * 创建开放 API 权限授权保存命令。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @param apiCode API 业务编码
     * @param apiName API 名称
     * @param httpMethod HTTP 方法
     * @param pathPattern 请求路径匹配规则
     * @param status 授权状态
     */
    public OpenApiPermissionSaveCommand(String tenantId, String appCode, String apiCode, String apiName,
                                        String httpMethod, String pathPattern, String status) {
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.apiCode = apiCode;
        this.apiName = apiName;
        this.httpMethod = httpMethod;
        this.pathPattern = pathPattern;
        this.status = status;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getApiName() {
        return apiName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public String getStatus() {
        return status;
    }
}
