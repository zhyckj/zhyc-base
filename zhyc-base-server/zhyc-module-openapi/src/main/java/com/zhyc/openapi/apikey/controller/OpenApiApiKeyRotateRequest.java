/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.controller;

import java.time.LocalDateTime;

/**
 * API Key Secret 轮换请求。
 */
public class OpenApiApiKeyRotateRequest {

    /** 租户业务编码，用于共享表模式下隔离 API Key。 */
    private String tenantId;
    /** 开发者应用编码，用于限定密钥所属应用。 */
    private String appCode;
    /** 新 API Secret 密文，只允许写入。 */
    private String secretCipher;
    /** 新凭证过期时间；为空时表示不设置固定过期时间。 */
    private LocalDateTime expireAt;

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 返回开发者应用编码。
     *
     * @return 开发者应用编码
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * 设置开发者应用编码。
     *
     * @param appCode 开发者应用编码
     */
    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    /**
     * 返回新 API Secret 密文。
     *
     * @return 新 API Secret 密文
     */
    public String getSecretCipher() {
        return secretCipher;
    }

    /**
     * 设置新 API Secret 密文。
     *
     * @param secretCipher 新 API Secret 密文
     */
    public void setSecretCipher(String secretCipher) {
        this.secretCipher = secretCipher;
    }

    /**
     * 返回新凭证过期时间。
     *
     * @return 新凭证过期时间
     */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /**
     * 设置新凭证过期时间。
     *
     * @param expireAt 新凭证过期时间
     */
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
}
