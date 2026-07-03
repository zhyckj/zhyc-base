/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.service;

/**
 * AI 调用审计记录命令。
 */
public class AiInvocationAuditRecordCommand {

    private final String tenantId;
    private final String appCode;
    private final Long providerId;
    private final Long modelId;
    private final String invocationType;
    private final int promptTokens;
    private final int completionTokens;
    private final int totalTokens;
    private final long latencyMs;
    private final String status;
    private final String errorMessage;
    private final String traceId;

    public AiInvocationAuditRecordCommand(String tenantId, String appCode, Long providerId, Long modelId,
                                          String invocationType, int promptTokens, int completionTokens,
                                          int totalTokens, long latencyMs, String status, String errorMessage,
                                          String traceId) {
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.providerId = providerId;
        this.modelId = modelId;
        this.invocationType = invocationType;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.latencyMs = latencyMs;
        this.status = status;
        this.errorMessage = errorMessage;
        this.traceId = traceId;
    }

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
