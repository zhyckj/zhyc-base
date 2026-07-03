/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.service;

/**
 * AI 运行时对话调用响应。
 *
 * @param appCode AI 应用编码
 * @param providerCode 模型供应商编码
 * @param modelCode 模型编码
 * @param content 模型输出内容
 * @param promptTokens 输入令牌数
 * @param completionTokens 输出令牌数
 * @param totalTokens 总令牌数
 * @param latencyMs 调用耗时，单位毫秒
 * @param traceId 调用追踪编号
 */
public record AiRuntimeChatResponse(String appCode, String providerCode, String modelCode, String content,
                                    int promptTokens, int completionTokens, int totalTokens, long latencyMs,
                                    String traceId) {
}
