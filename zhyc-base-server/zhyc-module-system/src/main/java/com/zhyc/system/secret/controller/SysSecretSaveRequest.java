/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.controller;

import java.time.LocalDateTime;

/**
 * 系统密钥保存请求。
 */
public class SysSecretSaveRequest {

    /** 密钥主键，更新时使用。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 密钥编码。 */
    private String secretCode;
    /** 密钥名称。 */
    private String secretName;
    /** 密钥类型。 */
    private String secretKind;
    /** 密钥明文，提交后由服务层立即加密。 */
    private String secretPlaintext;
    /** 密钥状态。 */
    private String status;
    /** 密钥到期时间。 */
    private LocalDateTime expireAt;

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

    /** @return 密钥明文 */
    public String getSecretPlaintext() {
        return secretPlaintext;
    }

    /** @param secretPlaintext 密钥明文 */
    public void setSecretPlaintext(String secretPlaintext) {
        this.secretPlaintext = secretPlaintext;
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
}
