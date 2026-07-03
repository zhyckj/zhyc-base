/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

/**
 * AI 供应商可用性测试命令。
 *
 * @param tenantId 租户业务编码
 * @param providerCode 供应商编码
 * @param providerType 供应商类型
 * @param baseUrl 基础地址
 * @param secretRef 密钥中心引用
 */
public record AiProviderTestCommand(String tenantId, String providerCode, String providerType,
                                    String baseUrl, String secretRef) {
}
