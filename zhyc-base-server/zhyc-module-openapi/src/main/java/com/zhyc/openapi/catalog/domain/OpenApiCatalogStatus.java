/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog.domain;

import java.util.Arrays;

/**
 * 开放 API 目录状态。
 *
 * <p>用于约束 API 目录是否可对开发者门户和网关运行态开放，避免目录管理接收不可识别值。</p>
 */
public enum OpenApiCatalogStatus {

    /** 启用状态，表示 API 目录可展示并可参与网关路由匹配。 */
    ENABLED("enabled", "启用"),
    /** 禁用状态，表示 API 目录暂不对外展示或参与调用。 */
    DISABLED("disabled", "禁用");

    /** API 目录状态编码，作为数据库持久化值和开放 API 管理契约值。 */
    private final String code;
    /** API 目录状态中文说明，用于开发者门户和后台管理端展示。 */
    private final String description;

    OpenApiCatalogStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 返回 API 目录状态编码。
     *
     * @return API 目录状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回 API 目录状态中文说明。
     *
     * @return API 目录状态中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析 API 目录状态。
     *
     * @param code API 目录状态编码
     * @return 匹配的 API 目录状态
     */
    public static OpenApiCatalogStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("API 目录状态只支持 enabled 或 disabled"));
    }
}
