/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug.controller;

/**
 * 开放 API 调试代理请求。
 */
public class OpenApiDebugInvokeRequest {

    /** 租户业务编码，用于调试代理校验租户边界。 */
    private String tenantId;
    /** 开放 API 业务编码。 */
    private String apiCode;
    /** HTTP 请求方法。 */
    private String method;
    /** 开放 API 网关路径。 */
    private String path;
    /** 调试认证方式，支持 API_KEY 和 OAUTH2。 */
    private String authMode;
    /** API Key Access Key。 */
    private String accessKey;
    /** API Key 签名时间戳。 */
    private String timestamp;
    /** API Key 签名随机串。 */
    private String nonce;
    /** API Key 签名值。 */
    private String signature;
    /** OAuth2/OIDC 访问令牌。 */
    private String bearerToken;
    /** 请求追踪编号。 */
    private String requestId;
    /** 请求体文本。 */
    private String body;

    /**
     * 获取租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 获取开放 API 业务编码。
     *
     * @return 开放 API 业务编码
     */
    public String getApiCode() {
        return apiCode;
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
     * 获取调试认证方式。
     *
     * @return 调试认证方式
     */
    public String getAuthMode() {
        return authMode;
    }

    /**
     * 获取 API Key Access Key。
     *
     * @return API Key Access Key
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * 获取 API Key 签名时间戳。
     *
     * @return API Key 签名时间戳
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * 获取 API Key 签名随机串。
     *
     * @return API Key 签名随机串
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * 获取 API Key 签名值。
     *
     * @return API Key 签名值
     */
    public String getSignature() {
        return signature;
    }

    /**
     * 获取 OAuth2/OIDC 访问令牌。
     *
     * @return OAuth2/OIDC 访问令牌
     */
    public String getBearerToken() {
        return bearerToken;
    }

    /**
     * 获取请求追踪编号。
     *
     * @return 请求追踪编号
     */
    public String getRequestId() {
        return requestId;
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
