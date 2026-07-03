/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version.service;

/**
 * 开放 API 版本发布响应。
 */
public class OpenApiVersionResponse {

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

    /**
     * 创建开放 API 版本发布响应。
     *
     * @param version API 版本号
     * @param backendRoute 后端转发路由
     * @param requestSchema 请求 JSON Schema
     * @param responseSchema 响应 JSON Schema
     * @param status API 版本状态
     */
    public OpenApiVersionResponse(String version, String backendRoute, String requestSchema,
                                  String responseSchema, String status) {
        this.version = version;
        this.backendRoute = backendRoute;
        this.requestSchema = requestSchema;
        this.responseSchema = responseSchema;
        this.status = status;
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
}
