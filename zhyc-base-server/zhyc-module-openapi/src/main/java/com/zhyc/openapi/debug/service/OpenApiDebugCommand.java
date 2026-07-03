/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug.service;

/**
 * 开放 API 调试代理命令。
 */
public class OpenApiDebugCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** 开放 API 业务编码。 */
    private final String apiCode;
    /** HTTP 请求方法。 */
    private final String method;
    /** 开放 API 网关路径。 */
    private final String path;
    /** 调试认证方式。 */
    private final String authMode;
    /** API Key Access Key。 */
    private final String accessKey;
    /** API Key 签名时间戳。 */
    private final String timestamp;
    /** API Key 签名随机串。 */
    private final String nonce;
    /** API Key 签名值。 */
    private final String signature;
    /** OAuth2/OIDC 访问令牌。 */
    private final String bearerToken;
    /** 请求追踪编号。 */
    private final String requestId;
    /** 请求体文本。 */
    private final String body;

    /**
     * 创建开放 API 调试代理命令。
     *
     * @param tenantId 租户业务编码
     * @param apiCode 开放 API 业务编码
     * @param method HTTP 请求方法
     * @param path 开放 API 网关路径
     * @param authMode 调试认证方式
     * @param accessKey API Key Access Key
     * @param timestamp API Key 签名时间戳
     * @param nonce API Key 签名随机串
     * @param signature API Key 签名值
     * @param bearerToken OAuth2/OIDC 访问令牌
     * @param requestId 请求追踪编号
     * @param body 请求体文本
     */
    public OpenApiDebugCommand(String tenantId, String apiCode, String method, String path, String authMode,
                               String accessKey, String timestamp, String nonce, String signature, String bearerToken,
                               String requestId, String body) {
        this.tenantId = tenantId;
        this.apiCode = apiCode;
        this.method = method;
        this.path = path;
        this.authMode = authMode;
        this.accessKey = accessKey;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.signature = signature;
        this.bearerToken = bearerToken;
        this.requestId = requestId;
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
