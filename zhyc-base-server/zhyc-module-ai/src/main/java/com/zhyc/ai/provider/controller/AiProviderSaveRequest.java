/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.controller;

/**
 * AI 模型供应商保存请求。
 */
public class AiProviderSaveRequest {

    private String tenantId;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String baseUrl;
    private String secretRef;
    private String status;

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
