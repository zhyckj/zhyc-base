/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.domain;

import java.util.Arrays;

/**
 * 开放 API Key 状态。
 *
 * <p>用于约束 API Key 生命周期状态，避免网关验签和密钥轮换流程接收不可识别值。</p>
 */
public enum OpenApiApiKeyStatus {

    /** 启用状态，允许网关使用该密钥进行签名校验。 */
    ENABLED("enabled", "启用"),
    /** 禁用状态，网关应拒绝该密钥发起的开放 API 调用。 */
    DISABLED("disabled", "禁用"),
    /** 已过期状态，表示密钥超过有效期后不可继续调用。 */
    EXPIRED("expired", "已过期");

    /** API Key 状态编码，作为数据库持久化值和开放 API 管理契约值。 */
    private final String code;
    /** API Key 状态中文说明，用于开发者门户和后台管理端展示。 */
    private final String description;

    OpenApiApiKeyStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 返回 API Key 状态编码。
     *
     * @return API Key 状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回 API Key 状态中文说明。
     *
     * @return API Key 状态中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析 API Key 状态。
     *
     * @param code API Key 状态编码
     * @return 匹配的开放 API Key 状态
     */
    public static OpenApiApiKeyStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("API Key 状态只支持 enabled、disabled 或 expired"));
    }
}
