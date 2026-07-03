/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.domain;

import java.time.LocalDateTime;

/**
 * API Key 领域对象。
 */
public class OpenApiApiKey {

    /** 主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 开发者应用编码。 */
    private String appCode;
    /** API 访问密钥。 */
    private String accessKey;
    /** API Secret 密文。 */
    private String secretCipher;
    /** API Key 状态。 */
    private String status;
    /** 凭证过期时间。 */
    private LocalDateTime expireAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建 API Key 领域对象。
     *
     * @param id 主键
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @param accessKey API 访问密钥
     * @param secretCipher API Secret 密文
     * @param status API Key 状态
     * @param expireAt 凭证过期时间
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public OpenApiApiKey(Long id, String tenantId, String appCode, String accessKey,
                         String secretCipher, String status, LocalDateTime expireAt,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.accessKey = accessKey;
        this.secretCipher = secretCipher;
        this.status = status;
        this.expireAt = expireAt;
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

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretCipher() {
        return secretCipher;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
