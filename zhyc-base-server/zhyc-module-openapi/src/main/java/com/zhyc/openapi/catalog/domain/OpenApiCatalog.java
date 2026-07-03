/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog.domain;

import java.time.LocalDateTime;

/**
 * 开放 API 目录领域对象。
 */
public class OpenApiCatalog {

    /** 主键。 */
    private Long id;
    /** API 业务编码。 */
    private String apiCode;
    /** API 名称。 */
    private String apiName;
    /** API 分组编码。 */
    private String groupCode;
    /** HTTP 方法。 */
    private String httpMethod;
    /** 请求路径匹配规则。 */
    private String pathPattern;
    /** API 目录状态。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建开放 API 目录领域对象。
     *
     * @param id 主键
     * @param apiCode API 业务编码
     * @param apiName API 名称
     * @param groupCode API 分组编码
     * @param httpMethod HTTP 方法
     * @param pathPattern 请求路径匹配规则
     * @param status API 目录状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public OpenApiCatalog(Long id, String apiCode, String apiName, String groupCode, String httpMethod,
                          String pathPattern, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.apiCode = apiCode;
        this.apiName = apiName;
        this.groupCode = groupCode;
        this.httpMethod = httpMethod;
        this.pathPattern = pathPattern;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getApiName() {
        return apiName;
    }

    public String getGroupCode() {
        return groupCode;
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
