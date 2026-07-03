/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.service;

import java.util.List;

/**
 * AI 模型供应商业务服务。
 */
public interface AiProviderService {

    /**
     * 查询租户模型供应商列表。
     *
     * @param tenantId 租户业务编码
     * @return 模型供应商列表
     */
    List<AiProviderResponse> listProviders(String tenantId);

    /**
     * 保存模型供应商。
     *
     * @param command 保存命令
     */
    void save(AiProviderSaveCommand command);
}
