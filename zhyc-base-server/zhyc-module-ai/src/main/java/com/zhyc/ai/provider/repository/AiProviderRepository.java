/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.repository;

import com.zhyc.ai.provider.domain.AiProvider;

import java.util.List;
import java.util.Optional;

/**
 * AI 模型供应商仓储。
 */
public interface AiProviderRepository {

    /**
     * 查询租户下的模型供应商。
     *
     * @param tenantId 租户业务编码
     * @return 模型供应商列表
     */
    List<AiProvider> findByTenantId(String tenantId);

    /**
     * 按租户和供应商主键查询模型供应商。
     *
     * @param tenantId 租户业务编码
     * @param id 供应商主键
     * @return 模型供应商
     */
    Optional<AiProvider> findByTenantIdAndId(String tenantId, Long id);

    /**
     * 保存模型供应商。
     *
     * @param provider 模型供应商
     */
    void save(AiProvider provider);
}
