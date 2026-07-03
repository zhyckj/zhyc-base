/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.repository;

import com.zhyc.ai.model.domain.AiModelConfig;

import java.util.List;
import java.util.Optional;

/**
 * AI 模型配置仓储。
 */
public interface AiModelConfigRepository {

    List<AiModelConfig> findByTenantId(String tenantId);

    /**
     * 按租户和模型主键查询模型配置。
     *
     * @param tenantId 租户业务编码
     * @param id 模型配置主键
     * @return 模型配置
     */
    Optional<AiModelConfig> findByTenantIdAndId(String tenantId, Long id);

    void save(AiModelConfig modelConfig);
}
