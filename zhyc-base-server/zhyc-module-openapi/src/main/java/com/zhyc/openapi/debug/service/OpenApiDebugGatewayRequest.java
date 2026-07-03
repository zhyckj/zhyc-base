/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug.service;

import java.util.Map;

/**
 * 开放 API 调试网关请求。
 */
public class OpenApiDebugGatewayRequest {

    /** 租户业务编码。 */
    private final String tenantId;
    /** HTTP 请求方法。 */
    private final String method;
    /** 开放 API 网关路径。 */
    private final String path;
    /** 网关请求头。 */
    private final Map<String, String> headers;
    /** 请求体文本。 */
    private final String body;

    /**
     * 创建开放 API 调试网关请求。
     *
     * @param tenantId 租户业务编码
     * @param method HTTP 请求方法
     * @param path 开放 API 网关路径
     * @param headers 网关请求头
     * @param body 请求体文本
     */
    public OpenApiDebugGatewayRequest(String tenantId, String method, String path, Map<String, String> headers,
                                      String body) {
        this.tenantId = tenantId;
        this.method = method;
        this.path = path;
        this.headers = Map.copyOf(headers);
        this.body = body;
    }

    /**
     * 获取租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 获取 HTTP 请求方法。
     *
     * @return HTTP 请求方法
     */
    public String getMethod() {
        return method;
    }

    /**
     * 获取开放 API 网关路径。
     *
     * @return 开放 API 网关路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取网关请求头。
     *
     * @return 网关请求头
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 获取请求体文本。
     *
     * @return 请求体文本
     */
    public String getBody() {
        return body;
    }
}
