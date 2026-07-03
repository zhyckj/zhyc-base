/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.domain;

/**
 * AI 模型供应商类型。
 */
public enum AiProviderType {

    /** OpenAI 兼容接口。 */
    OPENAI_COMPATIBLE("openai_compatible"),
    /** 通义千问。 */
    DASHSCOPE("dashscope"),
    /** 火山方舟。 */
    VOLCENGINE("volcengine"),
    /** DeepSeek。 */
    DEEPSEEK("deepseek"),
    /** 智谱。 */
    ZHIPU("zhipu"),
    /** 本地模型服务。 */
    LOCAL("local");

    private final String code;

    AiProviderType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 按编码解析供应商类型。
     *
     * @param code 供应商类型编码
     * @return 供应商类型
     */
    public static AiProviderType fromCode(String code) {
        for (AiProviderType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("AI 模型供应商类型不受支持");
    }
}
