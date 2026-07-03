/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version.domain;

import java.time.LocalDateTime;

/**
 * 开放 API 版本发布领域对象。
 */
public class OpenApiVersion {

    /** 主键。 */
    private Long id;
    /** API 业务编码。 */
    private String apiCode;
    /** API 版本号。 */
    private String version;
    /** 后端转发路由。 */
    private String backendRoute;
    /** 请求 JSON Schema。 */
    private String requestSchema;
    /** 响应 JSON Schema。 */
    private String responseSchema;
    /** API 版本状态。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建开放 API 版本发布领域对象。
     *
     * @param id 主键
     * @param apiCode API 业务编码
     * @param version API 版本号
     * @param backendRoute 后端转发路由
     * @param requestSchema 请求 JSON Schema
     * @param responseSchema 响应 JSON Schema
     * @param status API 版本状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public OpenApiVersion(Long id, String apiCode, String version, String backendRoute,
                          String requestSchema, String responseSchema, String status,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.apiCode = apiCode;
        this.version = version;
        this.backendRoute = backendRoute;
        this.requestSchema = requestSchema;
        this.responseSchema = responseSchema;
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

    public String getVersion() {
        return version;
    }

    public String getBackendRoute() {
        return backendRoute;
    }

    public String getRequestSchema() {
        return requestSchema;
    }

    public String getResponseSchema() {
        return responseSchema;
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
