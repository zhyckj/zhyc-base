/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.service;

import com.zhyc.system.secret.domain.SysSecret;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 系统密钥响应对象。
 *
 * <p>只返回密钥引用、状态和时间字段，不返回明文、密文或掩码内容。</p>
 */
public class SysSecretResponse {

    /** 密钥主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 密钥编码。 */
    private final String secretCode;
    /** 密钥引用。 */
    private final String secretRef;
    /** 密钥名称。 */
    private final String secretName;
    /** 密钥类型。 */
    private final String secretKind;
    /** 密钥状态。 */
    private final String status;
    /** 密钥到期时间。 */
    private final LocalDateTime expireAt;
    /** 最近轮换时间。 */
    private final LocalDateTime lastRotatedAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    /**
     * 创建系统密钥响应对象。
     *
     * @param id 密钥主键
     * @param tenantId 租户业务编码
     * @param secretCode 密钥编码
     * @param secretRef 密钥引用
     * @param secretName 密钥名称
     * @param secretKind 密钥类型
     * @param status 密钥状态
     * @param expireAt 密钥到期时间
     * @param lastRotatedAt 最近轮换时间
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysSecretResponse(Long id, String tenantId, String secretCode, String secretRef, String secretName,
                             String secretKind, String status, LocalDateTime expireAt,
                             LocalDateTime lastRotatedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.secretCode = secretCode;
        this.secretRef = secretRef;
        this.secretName = secretName;
        this.secretKind = secretKind;
        this.status = status;
        this.expireAt = expireAt;
        this.lastRotatedAt = lastRotatedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 由领域对象创建响应。
     *
     * @param secret 系统密钥领域对象
     * @return 系统密钥响应对象
     */
    public static SysSecretResponse from(SysSecret secret) {
        Objects.requireNonNull(secret, "系统密钥不能为空");
        String secretCode = secret.getSecretCode();
        return new SysSecretResponse(secret.getId(), secret.getTenantId(), secretCode, buildSecretRef(secretCode),
                secret.getSecretName(), secret.getSecretKind(), secret.getStatus(),
                secret.getExpireAt(), secret.getLastRotatedAt(), secret.getCreatedAt(), secret.getUpdatedAt());
    }

    /**
     * 返回密钥主键。
     *
     * @return 密钥主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 返回密钥编码。
     *
     * @return 密钥编码
     */
    public String getSecretCode() {
        return secretCode;
    }

    /**
     * 返回密钥引用。
     *
     * @return 密钥引用
     */
    public String getSecretRef() {
        return secretRef;
    }

    /**
     * 返回密钥名称。
     *
     * @return 密钥名称
     */
    public String getSecretName() {
        return secretName;
    }

    /**
     * 返回密钥类型。
     *
     * @return 密钥类型
     */
    public String getSecretKind() {
        return secretKind;
    }

    /**
     * 返回密钥状态。
     *
     * @return 密钥状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 返回密钥到期时间。
     *
     * @return 密钥到期时间
     */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /**
     * 返回最近轮换时间。
     *
     * @return 最近轮换时间
     */
    public LocalDateTime getLastRotatedAt() {
        return lastRotatedAt;
    }

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 返回更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static String buildSecretRef(String secretCode) {
        String normalized = secretCode == null ? "" : secretCode.trim();
        return "secret:" + normalized;
    }
}
