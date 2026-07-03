/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog.service;

/**
 * 开放 API 目录响应。
 */
public class OpenApiCatalogResponse {

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

    /**
     * 创建开放 API 目录响应。
     *
     * @param apiCode API 业务编码
     * @param apiName API 名称
     * @param groupCode API 分组编码
     * @param httpMethod HTTP 方法
     * @param pathPattern 请求路径匹配规则
     * @param status API 目录状态
     */
    public OpenApiCatalogResponse(String apiCode, String apiName, String groupCode, String httpMethod,
                                  String pathPattern, String status) {
        this.apiCode = apiCode;
        this.apiName = apiName;
        this.groupCode = groupCode;
        this.httpMethod = httpMethod;
        this.pathPattern = pathPattern;
        this.status = status;
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
}
