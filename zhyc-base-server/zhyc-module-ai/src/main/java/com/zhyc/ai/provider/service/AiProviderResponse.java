/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

import java.io.Serializable;

/**
 * AI 模型供应商响应对象。
 */
public class AiProviderResponse implements Serializable {

    /** 序列化版本号，用于 Redis 缓存反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 供应商主键；仅供前端作为下拉取值提交，不作为用户可编辑字段展示。 */
    private final Long id;
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

    public AiProviderResponse(Long id, String providerCode, String providerName, String providerType,
                              String baseUrl, String secretRef, String status) {
        this.id = id;
        this.providerCode = providerCode;
        this.providerName = providerName;
        this.providerType = providerType;
        this.baseUrl = baseUrl;
        this.secretRef = secretRef;
        this.status = status;
    }

    public Long getId() {
        return id;
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
