/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.repository;

import com.zhyc.ai.model.domain.AiModelConfig;
import com.zhyc.ai.model.mapper.AiModelConfigMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的 AI 模型配置仓储实现。
 */
@Repository
public class MyBatisAiModelConfigRepository implements AiModelConfigRepository {

    private final AiModelConfigMapper modelConfigMapper;

    public MyBatisAiModelConfigRepository(AiModelConfigMapper modelConfigMapper) {
        this.modelConfigMapper = Objects.requireNonNull(modelConfigMapper, "AI 模型配置 Mapper 不能为空");
    }

    @Override
    public List<AiModelConfig> findByTenantId(String tenantId) {
        return modelConfigMapper.selectByTenantId(tenantId);
    }

    @Override
    public Optional<AiModelConfig> findByTenantIdAndId(String tenantId, Long id) {
        return modelConfigMapper.selectByTenantIdAndId(tenantId, id);
    }

    @Override
    public void save(AiModelConfig modelConfig) {
        modelConfigMapper.upsert(modelConfig);
    }
}
