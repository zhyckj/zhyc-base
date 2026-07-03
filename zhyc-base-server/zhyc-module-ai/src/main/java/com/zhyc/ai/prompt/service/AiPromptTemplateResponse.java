/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.service;

import java.io.Serializable;

/**
 * AI 提示词模板响应。
 *
 * @param promptCode 提示词编码
 * @param promptName 提示词名称
 * @param version 版本号
 * @param templateContent 模板内容
 * @param variables 变量清单
 * @param status 状态
 */
public record AiPromptTemplateResponse(String promptCode, String promptName, String version,
                                       String templateContent, String variables, String status)
        implements Serializable {

    /** 序列化版本号，用于 Redis 缓存反序列化兼容。 */
    private static final long serialVersionUID = 1L;
}
