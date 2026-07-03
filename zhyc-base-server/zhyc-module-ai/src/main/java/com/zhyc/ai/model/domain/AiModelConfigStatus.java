/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.domain;

/**
 * AI 模型配置状态。
 */
public enum AiModelConfigStatus {

    /** 启用。 */
    ENABLED("enabled"),
    /** 停用。 */
    DISABLED("disabled");

    private final String code;

    AiModelConfigStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AiModelConfigStatus fromCode(String code) {
        for (AiModelConfigStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("AI 模型配置状态只支持 enabled 或 disabled");
    }
}
