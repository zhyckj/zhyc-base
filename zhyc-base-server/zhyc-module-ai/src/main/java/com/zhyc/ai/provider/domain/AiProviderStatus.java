/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.domain;

/**
 * AI 模型供应商状态。
 */
public enum AiProviderStatus {

    /** 启用。 */
    ENABLED("enabled"),
    /** 停用。 */
    DISABLED("disabled");

    private final String code;

    AiProviderStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 按编码解析供应商状态。
     *
     * @param code 状态编码
     * @return 供应商状态
     */
    public static AiProviderStatus fromCode(String code) {
        for (AiProviderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("AI 模型供应商状态只支持 enabled 或 disabled");
    }
}
