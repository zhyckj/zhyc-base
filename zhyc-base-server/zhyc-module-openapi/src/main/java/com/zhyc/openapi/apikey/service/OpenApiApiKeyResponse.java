/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.service;

import java.time.LocalDateTime;

/**
 * API Key 响应对象。
 */
public class OpenApiApiKeyResponse {

    /** 开发者应用编码。 */
    private final String appCode;
    /** API 访问密钥。 */
    private final String accessKey;
    /** API Secret 掩码。 */
    private final String secretMask;
    /** API Key 状态。 */
    private final String status;
    /** 凭证过期时间。 */
    private final LocalDateTime expireAt;

    /**
     * 创建 API Key 响应对象。
     *
     * @param appCode 开发者应用编码
     * @param accessKey API 访问密钥
     * @param secretMask API Secret 掩码
     * @param status API Key 状态
     * @param expireAt 凭证过期时间
     */
    public OpenApiApiKeyResponse(String appCode, String accessKey, String secretMask,
                                 String status, LocalDateTime expireAt) {
        this.appCode = appCode;
        this.accessKey = accessKey;
        this.secretMask = secretMask;
        this.status = status;
        this.expireAt = expireAt;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretMask() {
        return secretMask;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }
}
