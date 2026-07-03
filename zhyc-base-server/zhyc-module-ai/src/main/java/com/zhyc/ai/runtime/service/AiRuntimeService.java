/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.service;

/**
 * AI 运行时统一调用服务。
 */
public interface AiRuntimeService {

    /**
     * 按 AI 应用和提示词模板发起对话调用。
     *
     * @param command 对话调用命令
     * @return 对话调用响应
     */
    AiRuntimeChatResponse chat(AiRuntimeChatCommand command);
}
