/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature.domain;

import java.util.Arrays;

/**
 * 开放 API 签名算法。
 *
 * <p>首期与开放 API 网关运行态保持一致，只启用 HMAC-SHA256，后续国密或多算法扩展必须先补充网关校验器。</p>
 */
public enum OpenApiSignatureAlgorithm {

    /** HMAC-SHA256 签名算法，当前 API Key 签名运行态算法。 */
    HMAC_SHA256("HMAC_SHA256", "HMAC-SHA256");

    /** 签名算法编码，作为数据库持久化值和管理端契约值。 */
    private final String code;
    /** 签名算法中文说明，用于后台管理端展示。 */
    private final String description;

    OpenApiSignatureAlgorithm(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析签名算法。
     *
     * @param code 签名算法编码
     * @return 匹配的签名算法
     */
    public static OpenApiSignatureAlgorithm fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("签名算法只支持 HMAC_SHA256"));
    }
}
