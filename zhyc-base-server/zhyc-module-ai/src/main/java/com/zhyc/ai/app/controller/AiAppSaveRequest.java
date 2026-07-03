/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.controller;

/**
 * AI 应用接入保存请求。
 */
public class AiAppSaveRequest {

    private String tenantId;
    private String appCode;
    private String appName;
    private Long defaultModelId;
    private String systemPrompt;
    private int dailyTokenQuota;
    private String status;

    public String getTenantId() {
        return tenantId;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getAppName() {
        return appName;
    }

    public Long getDefaultModelId() {
        return defaultModelId;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public int getDailyTokenQuota() {
        return dailyTokenQuota;
    }

    public String getStatus() {
        return status;
    }
}
