/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.domain;

import java.util.Arrays;

/**
 * AI 应用状态枚举。
 */
public enum AiAppStatus {

    ENABLED("enabled"),
    DISABLED("disabled");

    private final String code;

    AiAppStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AiAppStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("AI 应用状态只支持 enabled 或 disabled"));
    }
}
