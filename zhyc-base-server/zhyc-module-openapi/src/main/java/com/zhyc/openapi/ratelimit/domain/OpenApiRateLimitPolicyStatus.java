/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit.domain;

import java.util.Arrays;

/**
 * 开放 API 限流策略状态。
 *
 * <p>用于约束租户、应用和接口维度的限流策略状态，避免网关限流运行态接收不可识别值。</p>
 */
public enum OpenApiRateLimitPolicyStatus {

    /** 启用状态，表示限流策略可参与开放 API 网关限流判断。 */
    ENABLED("enabled", "启用"),
    /** 禁用状态，表示限流策略暂不参与开放 API 网关限流判断。 */
    DISABLED("disabled", "禁用");

    /** 限流策略状态编码，作为数据库持久化值和开放 API 管理契约值。 */
    private final String code;
    /** 限流策略状态中文说明，用于开发者门户和后台管理端展示。 */
    private final String description;

    OpenApiRateLimitPolicyStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 返回限流策略状态编码。
     *
     * @return 限流策略状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回限流策略状态中文说明。
     *
     * @return 限流策略状态中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析限流策略状态。
     *
     * @param code 限流策略状态编码
     * @return 匹配的限流策略状态
     */
    public static OpenApiRateLimitPolicyStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("限流策略状态只支持 enabled 或 disabled"));
    }
}
