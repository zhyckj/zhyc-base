/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature.domain;

import java.util.Arrays;

/**
 * 开放 API 签名策略状态。
 *
 * <p>用于约束租户应用签名策略是否参与开放 API 网关签名校验。</p>
 */
public enum OpenApiSignaturePolicyStatus {

    /** 启用状态，表示签名策略参与开放 API 网关签名校验。 */
    ENABLED("enabled", "启用"),
    /** 禁用状态，表示签名策略暂不参与开放 API 网关签名校验。 */
    DISABLED("disabled", "禁用");

    /** 签名策略状态编码，作为数据库持久化值和管理端契约值。 */
    private final String code;
    /** 签名策略状态中文说明，用于后台管理端展示。 */
    private final String description;

    OpenApiSignaturePolicyStatus(String code, String description) {
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
     * 根据持久化编码解析签名策略状态。
     *
     * @param code 签名策略状态编码
     * @return 匹配的签名策略状态
     */
    public static OpenApiSignaturePolicyStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("签名策略状态只支持 enabled 或 disabled"));
    }
}
