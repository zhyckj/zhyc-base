/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.domain;

import java.util.Arrays;

/**
 * AI 提示词模板状态枚举。
 */
public enum AiPromptTemplateStatus {

    DRAFT("draft"),
    PUBLISHED("published"),
    DISABLED("disabled");

    private final String code;

    AiPromptTemplateStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AiPromptTemplateStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("AI 提示词模板状态只支持 draft、published 或 disabled"));
    }
}
