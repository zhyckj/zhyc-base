/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.domain;

import java.time.LocalDateTime;

/**
 * 系统密钥领域对象。
 *
 * <p>用于承载租户隔离下的密钥元数据、密文和状态信息，密文只允许通过服务层写入和轮换。</p>
 */
public class SysSecret {

    /** 密钥主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 密钥编码，同一租户内唯一。 */
    private String secretCode;
    /** 密钥名称。 */
    private String secretName;
    /** 密钥类型。 */
    private String secretKind;
    /** 密钥密文，只能由服务层写入。 */
    private String secretCipher;
    /** 密钥掩码，仅用于内部留痕和兼容历史数据，不向管理端响应输出。 */
    private String secretMask;
    /** 密钥状态。 */
    private String status;
    /** 密钥到期时间。 */
    private LocalDateTime expireAt;
    /** 最近轮换时间。 */
    private LocalDateTime lastRotatedAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建系统密钥领域对象。
     */
    public SysSecret() {
    }

    /** @return 密钥主键 */
    public Long getId() {
        return id;
    }

    /** @param id 密钥主键 */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return 租户业务编码 */
    public String getTenantId() {
        return tenantId;
    }

    /** @param tenantId 租户业务编码 */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /** @return 密钥编码 */
    public String getSecretCode() {
        return secretCode;
    }

    /** @param secretCode 密钥编码 */
    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    /** @return 密钥名称 */
    public String getSecretName() {
        return secretName;
    }

    /** @param secretName 密钥名称 */
    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }

    /** @return 密钥类型 */
    public String getSecretKind() {
        return secretKind;
    }

    /** @param secretKind 密钥类型 */
    public void setSecretKind(String secretKind) {
        this.secretKind = secretKind;
    }

    /** @return 密钥密文 */
    public String getSecretCipher() {
        return secretCipher;
    }

    /** @param secretCipher 密钥密文 */
    public void setSecretCipher(String secretCipher) {
        this.secretCipher = secretCipher;
    }

    /** @return 密钥掩码 */
    public String getSecretMask() {
        return secretMask;
    }

    /** @param secretMask 密钥掩码 */
    public void setSecretMask(String secretMask) {
        this.secretMask = secretMask;
    }

    /** @return 密钥状态 */
    public String getStatus() {
        return status;
    }

    /** @param status 密钥状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @return 密钥到期时间 */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /** @param expireAt 密钥到期时间 */
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    /** @return 最近轮换时间 */
    public LocalDateTime getLastRotatedAt() {
        return lastRotatedAt;
    }

    /** @param lastRotatedAt 最近轮换时间 */
    public void setLastRotatedAt(LocalDateTime lastRotatedAt) {
        this.lastRotatedAt = lastRotatedAt;
    }

    /** @return 创建时间 */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @param createdAt 创建时间 */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** @return 更新时间 */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** @param updatedAt 更新时间 */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
