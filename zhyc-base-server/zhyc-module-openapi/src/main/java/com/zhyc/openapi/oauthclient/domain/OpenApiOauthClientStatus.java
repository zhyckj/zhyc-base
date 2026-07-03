/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.domain;

import java.util.Arrays;

/**
 * 开放 API OAuth2 客户端映射状态。
 *
 * <p>用于约束开发者应用与认证中心 OAuth2 客户端的映射状态，避免授权校验接收不可识别值。</p>
 */
public enum OpenApiOauthClientStatus {

    /** 启用状态，允许该 OAuth2 客户端映射参与第三方应用授权校验。 */
    ENABLED("enabled", "启用"),
    /** 禁用状态，授权校验应拒绝该 OAuth2 客户端映射。 */
    DISABLED("disabled", "禁用");

    /** OAuth2 客户端映射状态编码，作为数据库持久化值和开放 API 管理契约值。 */
    private final String code;
    /** OAuth2 客户端映射状态中文说明，用于开发者门户和后台管理端展示。 */
    private final String description;

    OpenApiOauthClientStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 返回 OAuth2 客户端映射状态编码。
     *
     * @return OAuth2 客户端映射状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回 OAuth2 客户端映射状态中文说明。
     *
     * @return OAuth2 客户端映射状态中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析 OAuth2 客户端映射状态。
     *
     * @param code OAuth2 客户端映射状态编码
     * @return 匹配的 OAuth2 客户端映射状态
     */
    public static OpenApiOauthClientStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("OAuth2 客户端映射状态只支持 enabled 或 disabled"));
    }
}
