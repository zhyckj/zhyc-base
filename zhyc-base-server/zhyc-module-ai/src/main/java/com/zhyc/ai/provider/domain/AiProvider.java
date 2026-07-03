/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.domain;

import java.time.LocalDateTime;

/**
 * AI 模型供应商领域对象。
 */
public class AiProvider {

    /** 主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 供应商编码。 */
    private final String providerCode;
    /** 供应商名称。 */
    private final String providerName;
    /** 供应商类型。 */
    private final String providerType;
    /** 模型服务基础地址。 */
    private final String baseUrl;
    /** 密钥中心引用。 */
    private final String secretRef;
    /** 供应商状态。 */
    private final String status;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    public AiProvider(Long id, String tenantId, String providerCode, String providerName, String providerType,
                      String baseUrl, String secretRef, String status,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.providerCode = providerCode;
        this.providerName = providerName;
        this.providerType = providerType;
        this.baseUrl = baseUrl;
        this.secretRef = secretRef;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
