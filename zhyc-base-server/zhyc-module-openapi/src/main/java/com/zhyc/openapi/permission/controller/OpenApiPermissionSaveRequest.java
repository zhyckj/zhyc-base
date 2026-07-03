/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission.controller;

/**
 * 开放 API 权限授权保存请求。
 */
public class OpenApiPermissionSaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 开发者应用编码。 */
    private String appCode;
    /** API 业务编码。 */
    private String apiCode;
    /** API 名称。 */
    private String apiName;
    /** HTTP 方法。 */
    private String httpMethod;
    /** 请求路径匹配规则。 */
    private String pathPattern;
    /** 授权状态。 */
    private String status;

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
