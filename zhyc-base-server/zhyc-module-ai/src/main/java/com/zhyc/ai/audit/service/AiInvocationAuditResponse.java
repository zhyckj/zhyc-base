/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.service;

/**
 * AI 调用审计响应。
 *
 * @param appCode 应用编码
 * @param providerId 供应商主键
 * @param modelId 模型主键
 * @param invocationType 调用类型
 * @param promptTokens 提示词令牌数
 * @param completionTokens 输出令牌数
 * @param totalTokens 总令牌数
 * @param latencyMs 调用耗时毫秒
 * @param status 调用状态
 * @param errorMessage 错误消息
 * @param traceId 链路追踪编号
 */
public record AiInvocationAuditResponse(String appCode, Long providerId, Long modelId, String invocationType,
                                        int promptTokens, int completionTokens, int totalTokens, long latencyMs,
                                        String status, String errorMessage, String traceId) {
}
