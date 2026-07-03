/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.domain;

import java.util.Arrays;

/**
 * 开放 API 应用状态。
 *
 * <p>用于约束开发者应用的启停状态，避免网关鉴权、限流和授权判断接收不可识别值。</p>
 */
public enum OpenApiAppStatus {

    /** 启用状态，允许网关按授权和限流规则处理调用。 */
    ENABLED("enabled", "启用"),
    /** 禁用状态，网关应拒绝该应用发起的开放 API 调用。 */
    DISABLED("disabled", "禁用");

    /** 应用状态编码，作为数据库持久化值和开放 API 管理契约值。 */
    private final String code;
    /** 应用状态中文说明，用于开发者门户和后台管理端展示。 */
    private final String description;

    OpenApiAppStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 返回应用状态编码。
     *
     * @return 应用状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回应用状态中文说明。
     *
     * @return 应用状态中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析应用状态。
     *
     * @param code 应用状态编码
     * @return 匹配的开放 API 应用状态
     */
    public static OpenApiAppStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("应用状态只支持 enabled 或 disabled"));
    }
}
