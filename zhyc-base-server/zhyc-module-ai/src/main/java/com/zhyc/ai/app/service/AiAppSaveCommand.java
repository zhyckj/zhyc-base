/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.service;

/**
 * AI 应用接入保存命令。
 */
public class AiAppSaveCommand {

    private final String tenantId;
    private final String appCode;
    private final String appName;
    private final Long defaultModelId;
    private final String systemPrompt;
    private final int dailyTokenQuota;
    private final String status;

    public AiAppSaveCommand(String tenantId, String appCode, String appName, Long defaultModelId, String systemPrompt,
                            int dailyTokenQuota, String status) {
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.appName = appName;
        this.defaultModelId = defaultModelId;
        this.systemPrompt = systemPrompt;
        this.dailyTokenQuota = dailyTokenQuota;
        this.status = status;
    }

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
