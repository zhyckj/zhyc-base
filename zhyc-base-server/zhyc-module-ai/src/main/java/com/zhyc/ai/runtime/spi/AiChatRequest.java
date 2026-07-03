/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.spi;

/**
 * AI 对话模型请求。
 *
 * @param tenantId 租户业务编码
 * @param appCode AI 应用编码
 * @param providerCode 模型供应商编码
 * @param providerBaseUrl 模型供应商基础地址
 * @param modelCode 模型编码
 * @param systemPrompt 应用系统提示词
 * @param userPrompt 渲染后的用户提示词
 * @param stream 是否请求流式输出
 */
public record AiChatRequest(String tenantId, String appCode, String providerCode, String providerBaseUrl,
                            String modelCode, String systemPrompt, String userPrompt, boolean stream) {
}
