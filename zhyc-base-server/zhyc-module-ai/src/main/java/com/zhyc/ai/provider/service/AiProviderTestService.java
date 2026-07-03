/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

/**
 * AI 供应商可用性测试服务。
 */
public interface AiProviderTestService {

    /**
     * 测试供应商配置是否可用。
     *
     * @param command 测试命令
     * @return 测试结果
     */
    AiProviderTestResponse test(AiProviderTestCommand command);
}
