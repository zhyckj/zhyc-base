/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit.domain;

import java.time.LocalDateTime;

/**
 * 开放 API 调用审计领域对象。
 */
public class OpenApiCallAudit {

    /** 主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 开发者应用编码。 */
    private String appCode;
    /** API 访问密钥。 */
    private String accessKey;
    /** API 业务编码。 */
    private String apiCode;
    /** HTTP 方法。 */
    private String httpMethod;
    /** 请求路径。 */
    private String requestPath;
    /** HTTP 响应状态码。 */
    private Integer responseStatus;
    /** 调用耗时毫秒。 */
    private Long durationMs;
    /** 是否调用成功，1 是 0 否。 */
    private Integer success;
    /** 错误编码。 */
    private String errorCode;
    /** 客户端 IP。 */
    private String clientIp;
    /** 请求追踪 ID。 */
    private String requestId;
    /** 调用时间。 */
    private LocalDateTime calledAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建开放 API 调用审计领域对象。
     *
     * @param id 主键
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @param accessKey API 访问密钥
     * @param apiCode API 业务编码
     * @param httpMethod HTTP 方法
     * @param requestPath 请求路径
     * @param responseStatus HTTP 响应状态码
     * @param durationMs 调用耗时毫秒
     * @param success 是否调用成功，1 是 0 否
     * @param errorCode 错误编码
     * @param clientIp 客户端 IP
     * @param requestId 请求追踪 ID
     * @param calledAt 调用时间
     * @param createdAt 创建时间
     */
    public OpenApiCallAudit(Long id, String tenantId, String appCode, String accessKey,
                            String apiCode, String httpMethod, String requestPath,
                            Integer responseStatus, Long durationMs, Integer success,
                            String errorCode, String clientIp, String requestId,
                            LocalDateTime calledAt, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.accessKey = accessKey;
        this.apiCode = apiCode;
        this.httpMethod = httpMethod;
        this.requestPath = requestPath;
        this.responseStatus = responseStatus;
        this.durationMs = durationMs;
        this.success = success;
        this.errorCode = errorCode;
        this.clientIp = clientIp;
        this.requestId = requestId;
        this.calledAt = calledAt;
        this.createdAt = createdAt;
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

    public String getAccessKey() {
        return accessKey;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public Integer getSuccess() {
        return success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getCalledAt() {
        return calledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
