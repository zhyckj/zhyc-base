/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.domain;

import java.util.Arrays;

/**
 * 开放 API 应用鉴权方式。
 *
 * <p>用于约束开发者应用可选择的认证模式，保证网关只接收 API Key、OAuth2/OIDC 或组合模式。</p>
 */
public enum OpenApiAppAuthMode {

    /** API Key 鉴权方式，面向系统集成调用。 */
    API_KEY("api_key", "API Key 鉴权"),
    /** OAuth2/OIDC 鉴权方式，面向第三方应用授权调用。 */
    OAUTH2("oauth2", "OAuth2/OIDC 鉴权"),
    /** 同时支持 API Key 与 OAuth2/OIDC 的组合鉴权方式。 */
    BOTH("both", "API Key 与 OAuth2/OIDC 组合鉴权");

    /** 鉴权方式编码，作为数据库持久化值和开放 API 管理契约值。 */
    private final String code;
    /** 鉴权方式中文说明，用于开发者门户和后台管理端展示。 */
    private final String description;

    OpenApiAppAuthMode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 返回鉴权方式编码。
     *
     * @return 鉴权方式编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回鉴权方式中文说明。
     *
     * @return 鉴权方式中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析鉴权方式。
     *
     * @param code 鉴权方式编码
     * @return 匹配的开放 API 应用鉴权方式
     */
    public static OpenApiAppAuthMode fromCode(String code) {
        return Arrays.stream(values())
                .filter(mode -> mode.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("鉴权方式只支持 api_key、oauth2 或 both"));
    }
}
