/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version.domain;

import java.util.Arrays;

/**
 * 开放 API 版本状态。
 *
 * <p>用于约束 API 版本发布生命周期，避免网关路由和开发者门户读取不可识别状态。</p>
 */
public enum OpenApiVersionStatus {

    /** 草稿状态，表示版本尚未正式对外发布。 */
    DRAFT("draft", "草稿"),
    /** 已发布状态，表示版本可被开放 API 网关路由和开发者门户展示。 */
    PUBLISHED("published", "已发布"),
    /** 已下线状态，表示版本不再对外提供调用。 */
    OFFLINE("offline", "已下线");

    /** API 版本状态编码，作为数据库持久化值和开放 API 管理契约值。 */
    private final String code;
    /** API 版本状态中文说明，用于开发者门户和后台管理端展示。 */
    private final String description;

    OpenApiVersionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 返回 API 版本状态编码。
     *
     * @return API 版本状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回 API 版本状态中文说明。
     *
     * @return API 版本状态中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析 API 版本状态。
     *
     * @param code API 版本状态编码
     * @return 匹配的 API 版本状态
     */
    public static OpenApiVersionStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("API 版本状态只支持 draft、published 或 offline"));
    }
}
