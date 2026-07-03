/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.controller;

import java.time.LocalDateTime;

/**
 * 系统密钥轮换请求。
 */
public class SysSecretRotateRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 新密钥明文。 */
    private String secretPlaintext;
    /** 新到期时间。 */
    private LocalDateTime expireAt;

    /** @return 租户业务编码 */
    public String getTenantId() {
        return tenantId;
    }

    /** @param tenantId 租户业务编码 */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /** @return 新密钥明文 */
    public String getSecretPlaintext() {
        return secretPlaintext;
    }

    /** @param secretPlaintext 新密钥明文 */
    public void setSecretPlaintext(String secretPlaintext) {
        this.secretPlaintext = secretPlaintext;
    }

    /** @return 新到期时间 */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /** @param expireAt 新到期时间 */
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
}
