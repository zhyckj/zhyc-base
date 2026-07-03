/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.repository;

import com.zhyc.ai.provider.domain.AiProvider;
import com.zhyc.ai.provider.mapper.AiProviderMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的 AI 模型供应商仓储实现。
 */
@Repository
public class MyBatisAiProviderRepository implements AiProviderRepository {

    private final AiProviderMapper providerMapper;

    public MyBatisAiProviderRepository(AiProviderMapper providerMapper) {
        this.providerMapper = Objects.requireNonNull(providerMapper, "AI 模型供应商 Mapper 不能为空");
    }

    @Override
    public List<AiProvider> findByTenantId(String tenantId) {
        return providerMapper.selectByTenantId(tenantId);
    }

    @Override
    public Optional<AiProvider> findByTenantIdAndId(String tenantId, Long id) {
        return providerMapper.selectByTenantIdAndId(tenantId, id);
    }

    @Override
    public void save(AiProvider provider) {
        providerMapper.upsert(provider);
    }
}
