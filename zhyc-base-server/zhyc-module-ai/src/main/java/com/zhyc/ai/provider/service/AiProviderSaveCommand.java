/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

/**
 * AI 模型供应商保存命令。
 */
public class AiProviderSaveCommand {

    private final String tenantId;
    private final String providerCode;
    private final String providerName;
    private final String providerType;
    private final String baseUrl;
    private final String secretRef;
    private final String status;

    public AiProviderSaveCommand(String tenantId, String providerCode, String providerName, String providerType,
                                 String baseUrl, String secretRef, String status) {
        this.tenantId = tenantId;
        this.providerCode = providerCode;
        this.providerName = providerName;
        this.providerType = providerType;
        this.baseUrl = baseUrl;
        this.secretRef = secretRef;
        this.status = status;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getProviderType() {
        return providerType;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getSecretRef() {
        return secretRef;
    }

    public String getStatus() {
        return status;
    }
}
