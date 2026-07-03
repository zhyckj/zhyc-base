/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.repository;

import com.zhyc.ai.prompt.domain.AiPromptTemplate;
import com.zhyc.ai.prompt.mapper.AiPromptTemplateMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的 AI 提示词模板仓储实现。
 */
@Repository
public class MyBatisAiPromptTemplateRepository implements AiPromptTemplateRepository {

    private final AiPromptTemplateMapper templateMapper;

    public MyBatisAiPromptTemplateRepository(AiPromptTemplateMapper templateMapper) {
        this.templateMapper = Objects.requireNonNull(templateMapper, "AI 提示词模板 Mapper 不能为空");
    }

    @Override
    public List<AiPromptTemplate> findByTenantId(String tenantId) {
        return templateMapper.selectByTenantId(tenantId);
    }

    @Override
    public Optional<AiPromptTemplate> findByTenantIdAndPromptCodeAndVersion(String tenantId, String promptCode,
                                                                            String version) {
        return templateMapper.selectByTenantIdAndPromptCodeAndVersion(tenantId, promptCode, version);
    }

    @Override
    public void save(AiPromptTemplate template) {
        templateMapper.upsert(template);
    }
}
