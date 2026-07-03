/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

/**
 * AI 供应商可用性测试响应。
 *
 * @param providerCode 供应商编码
 * @param success 是否可用
 * @param latencyMs 测试耗时，单位毫秒
 * @param message 测试结果说明
 */
public record AiProviderTestResponse(String providerCode, boolean success, long latencyMs, String message) {
}
