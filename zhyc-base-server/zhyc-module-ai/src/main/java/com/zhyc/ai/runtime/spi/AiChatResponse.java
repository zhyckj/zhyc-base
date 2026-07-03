/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.spi;

/**
 * AI 对话模型响应。
 *
 * @param content 模型输出内容
 * @param promptTokens 输入令牌数
 * @param completionTokens 输出令牌数
 * @param totalTokens 总令牌数
 * @param traceId 供应商请求追踪编号
 */
public record AiChatResponse(String content, int promptTokens, int completionTokens, int totalTokens, String traceId) {
}
