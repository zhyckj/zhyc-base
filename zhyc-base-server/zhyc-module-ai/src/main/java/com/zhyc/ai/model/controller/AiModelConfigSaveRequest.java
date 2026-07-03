/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.controller;

/**
 * AI 模型配置保存请求。
 */
public class AiModelConfigSaveRequest {

    private String tenantId;
    private Long providerId;
    private String modelCode;
    private String modelName;
    private String modelType;
    private int contextWindow;
    private boolean supportStream;
    private boolean supportTool;
    private String status;

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
}
