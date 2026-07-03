/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.service;

import java.time.LocalDateTime;

/**
 * 系统密钥保存命令。
 */
public class SysSecretSaveCommand {

    /** 密钥主键，空值表示新增。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 密钥编码。 */
    private final String secretCode;
    /** 密钥名称。 */
    private final String secretName;
    /** 密钥类型。 */
    private final String secretKind;
    /** 密钥明文。 */
    private final String secretPlaintext;
    /** 密钥状态。 */
    private final String status;
    /** 密钥到期时间。 */
    private final LocalDateTime expireAt;

    /**
     * 创建系统密钥保存命令。
     *
     * @param id 密钥主键，空值表示新增
     * @param tenantId 租户业务编码
     * @param secretCode 密钥编码
     * @param secretName 密钥名称
     * @param secretKind 密钥类型
     * @param secretPlaintext 密钥明文
     * @param status 密钥状态
     * @param expireAt 密钥到期时间
     */
    public SysSecretSaveCommand(Long id, String tenantId, String secretCode, String secretName, String secretKind,
                                String secretPlaintext, String status, LocalDateTime expireAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.secretCode = secretCode;
        this.secretName = secretName;
        this.secretKind = secretKind;
        this.secretPlaintext = secretPlaintext;
        this.status = status;
        this.expireAt = expireAt;
    }

    /** @return 密钥主键 */
    public Long getId() {
        return id;
    }

    /** @return 租户业务编码 */
    public String getTenantId() {
        return tenantId;
    }

    /** @return 密钥编码 */
    public String getSecretCode() {
        return secretCode;
    }

    /** @return 密钥名称 */
    public String getSecretName() {
        return secretName;
    }

    /** @return 密钥类型 */
    public String getSecretKind() {
        return secretKind;
    }

    /** @return 密钥明文 */
    public String getSecretPlaintext() {
        return secretPlaintext;
    }

    /** @return 密钥状态 */
    public String getStatus() {
        return status;
    }

    /** @return 密钥到期时间 */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }
}
