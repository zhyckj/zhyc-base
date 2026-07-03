/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.controller;

import java.time.LocalDateTime;

/**
 * API Key 保存请求。
 */
public class OpenApiApiKeySaveRequest {

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
}
