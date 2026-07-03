/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.repository;

import com.zhyc.ai.prompt.domain.AiPromptTemplate;

import java.util.List;
import java.util.Optional;

/**
 * AI 提示词模板仓储。
 */
public interface AiPromptTemplateRepository {

    List<AiPromptTemplate> findByTenantId(String tenantId);

    Optional<AiPromptTemplate> findByTenantIdAndPromptCodeAndVersion(String tenantId, String promptCode,
                                                                     String version);

    void save(AiPromptTemplate template);
}
