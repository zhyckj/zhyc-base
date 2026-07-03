/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.domain;

import java.time.LocalDateTime;

/**
 * AI 模型配置领域对象。
 */
public class AiModelConfig {

    private final Long id;
    private final String tenantId;
    private final Long providerId;
    private final String modelCode;
    private final String modelName;
    private final String modelType;
    private final int contextWindow;
    private final boolean supportStream;
    private final boolean supportTool;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AiModelConfig(Long id, String tenantId, Long providerId, String modelCode, String modelName,
                         String modelType, int contextWindow, boolean supportStream, boolean supportTool,
                         String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.providerId = providerId;
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.modelType = modelType;
        this.contextWindow = contextWindow;
        this.supportStream = supportStream;
        this.supportTool = supportTool;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public String getModelCode() {
        return modelCode;
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelType() {
        return modelType;
    }

    public int getContextWindow() {
        return contextWindow;
    }

    public boolean isSupportStream() {
        return supportStream;
    }

    public boolean isSupportTool() {
        return supportTool;
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
