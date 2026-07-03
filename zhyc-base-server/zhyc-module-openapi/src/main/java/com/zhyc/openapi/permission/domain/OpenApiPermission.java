/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission.domain;

import java.time.LocalDateTime;

/**
 * 开放 API 权限授权领域对象。
 */
public class OpenApiPermission {

    /** 主键。 */
    private Long id;
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
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建开放 API 权限授权领域对象。
     *
     * @param id 主键
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @param apiCode API 业务编码
     * @param apiName API 名称
     * @param httpMethod HTTP 方法
     * @param pathPattern 请求路径匹配规则
     * @param status 授权状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public OpenApiPermission(Long id, String tenantId, String appCode, String apiCode, String apiName,
                             String httpMethod, String pathPattern, String status,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.apiCode = apiCode;
        this.apiName = apiName;
        this.httpMethod = httpMethod;
        this.pathPattern = pathPattern;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
