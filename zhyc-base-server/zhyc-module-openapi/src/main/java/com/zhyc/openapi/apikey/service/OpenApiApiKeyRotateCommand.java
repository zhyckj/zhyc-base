/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.service;

import java.time.LocalDateTime;

/**
 * API Key Secret 轮换命令。
 *
 * <p>只承载新 Secret 密文和定位密钥所需的租户、应用、Access Key，避免轮换流程读取或返回旧 Secret 明文。</p>
 */
public class OpenApiApiKeyRotateCommand {

    /** 租户业务编码，用于共享表模式下隔离 API Key。 */
    private final String tenantId;
    /** 开发者应用编码，用于限定密钥所属应用。 */
    private final String appCode;
    /** API 访问密钥，用于定位被轮换的凭证。 */
    private final String accessKey;
    /** 新 API Secret 密文，只允许写入，不允许通过列表接口返回明文。 */
    private final String secretCipher;
    /** 新凭证过期时间；为空时表示不设置固定过期时间。 */
    private final LocalDateTime expireAt;

    /**
     * 创建 API Key Secret 轮换命令。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @param accessKey API 访问密钥
     * @param secretCipher 新 API Secret 密文
     * @param expireAt 新凭证过期时间
     */
    public OpenApiApiKeyRotateCommand(String tenantId, String appCode, String accessKey,
                                      String secretCipher, LocalDateTime expireAt) {
        this.tenantId = tenantId;
        this.appCode = appCode;
        this.accessKey = accessKey;
        this.secretCipher = secretCipher;
        this.expireAt = expireAt;
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
     * 返回开发者应用编码。
     *
     * @return 开发者应用编码
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * 返回 API 访问密钥。
     *
     * @return API 访问密钥
     */
    public String getAccessKey() {
        return accessKey;
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
     * 返回新凭证过期时间。
     *
     * @return 新凭证过期时间
     */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }
}
