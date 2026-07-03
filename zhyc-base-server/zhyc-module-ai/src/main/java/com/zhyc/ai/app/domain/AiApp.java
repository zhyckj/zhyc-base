/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.domain;

import java.time.LocalDateTime;

/**
 * AI 应用接入领域对象。
 */
public class AiApp {

    /** 主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 应用编码。 */
    private final String appCode;
    /** 应用名称。 */
    private final String appName;
    /** 默认模型配置主键。 */
    private final Long defaultModelId;
    /** 系统提示词。 */
    private final String systemPrompt;
    /** 每日令牌额度。 */
    private final int dailyTokenQuota;
    /** 应用状态。 */
    private final String status;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    public AiApp(Long id, String tenantId, String appCode, String appName, Long defaultModelId, String systemPrompt,
                 int dailyTokenQuota, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.appName = appName;
        this.defaultModelId = defaultModelId;
        this.systemPrompt = systemPrompt;
        this.dailyTokenQuota = dailyTokenQuota;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
