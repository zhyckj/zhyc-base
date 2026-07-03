/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission.domain;

import java.util.Arrays;

/**
 * 开放 API 授权状态。
 *
 * <p>用于约束开发者应用的 API 授权规则状态，避免网关权限校验接收不可识别值。</p>
 */
public enum OpenApiPermissionStatus {

    /** 启用状态，表示授权规则可参与开放 API 网关权限校验。 */
    ENABLED("enabled", "启用"),
    /** 禁用状态，表示授权规则暂不参与开放 API 网关权限校验。 */
    DISABLED("disabled", "禁用");

    /** 授权状态编码，作为数据库持久化值和开放 API 管理契约值。 */
    private final String code;
    /** 授权状态中文说明，用于开发者门户和后台管理端展示。 */
    private final String description;

    OpenApiPermissionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 返回开放 API 授权状态编码。
     *
     * @return 开放 API 授权状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回开放 API 授权状态中文说明。
     *
     * @return 开放 API 授权状态中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析开放 API 授权状态。
     *
     * @param code 开放 API 授权状态编码
     * @return 匹配的开放 API 授权状态
     */
    public static OpenApiPermissionStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("授权状态只支持 enabled 或 disabled"));
    }
}
