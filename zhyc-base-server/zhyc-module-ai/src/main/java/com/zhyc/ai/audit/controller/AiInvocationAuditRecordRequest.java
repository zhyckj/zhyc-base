/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.controller;

/**
 * AI 调用审计记录请求。
 */
public class AiInvocationAuditRecordRequest {

    private String tenantId;
    private String appCode;
    private Long providerId;
    private Long modelId;
    private String invocationType;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private long latencyMs;
    private String status;
    private String errorMessage;
    private String traceId;

    public String getTenantId() {
        return tenantId;
    }

    public String getAppCode() {
        return appCode;
    }

    public Long getProviderId() {
        return providerId;
    }

    public Long getModelId() {
        return modelId;
    }

    public String getInvocationType() {
        return invocationType;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getTraceId() {
        return traceId;
    }
}
