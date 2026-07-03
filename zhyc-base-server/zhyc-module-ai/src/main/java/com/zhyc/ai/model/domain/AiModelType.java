/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.domain;

/**
 * AI 模型类型。
 */
public enum AiModelType {

    /** 对话模型。 */
    CHAT("chat"),
    /** 向量模型。 */
    EMBEDDING("embedding"),
    /** 多模态模型。 */
    MULTIMODAL("multimodal");

    private final String code;

    AiModelType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AiModelType fromCode(String code) {
        for (AiModelType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("AI 模型类型只支持 chat、embedding 或 multimodal");
    }
}
