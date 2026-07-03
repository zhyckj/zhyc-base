/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.support;

import java.util.Arrays;
import java.util.Locale;

/**
 * 开放 API 支持的 HTTP 方法。
 *
 * <p>用于统一约束 API 目录、授权规则和调用审计的 HTTP 方法取值，避免不同模块维护重复魔法字符串。</p>
 */
public enum OpenApiHttpMethod {

    /** GET 方法，通常用于资源查询。 */
    GET("GET", "查询"),
    /** POST 方法，通常用于资源创建或动作提交。 */
    POST("POST", "提交"),
    /** PUT 方法，通常用于资源整体更新。 */
    PUT("PUT", "更新"),
    /** DELETE 方法，通常用于资源删除或作废。 */
    DELETE("DELETE", "删除"),
    /** PATCH 方法，通常用于资源局部更新。 */
    PATCH("PATCH", "局部更新");

    /** HTTP 方法编码，作为数据库持久化值和开放 API 管理契约值。 */
    private final String code;
    /** HTTP 方法中文说明，用于后台管理端和开发者门户展示。 */
    private final String description;

    OpenApiHttpMethod(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 返回 HTTP 方法编码。
     *
     * @return HTTP 方法编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回 HTTP 方法中文说明。
     *
     * @return HTTP 方法中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据输入编码解析开放 API HTTP 方法。
     *
     * <p>输入编码会统一转换为大写后匹配，便于后台管理端表单和代码生成模板传入小写方法名。</p>
     *
     * @param code HTTP 方法编码
     * @return 匹配的开放 API HTTP 方法
     */
    public static OpenApiHttpMethod fromCode(String code) {
        String normalized = code.toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(method -> method.code.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("HTTP 方法只支持 GET、POST、PUT、DELETE 或 PATCH"));
    }
}
