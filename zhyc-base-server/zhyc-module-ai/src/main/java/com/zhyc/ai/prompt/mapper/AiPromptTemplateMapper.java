/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.mapper;

import com.zhyc.ai.prompt.domain.AiPromptTemplate;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Optional;

/**
 * AI 提示词模板 MyBatis Mapper。
 */
@Mapper
public interface AiPromptTemplateMapper {

    @SelectProvider(type = AiPromptTemplateSqlProvider.class, method = "selectByTenantId")
    List<AiPromptTemplate> selectByTenantId(@Param("tenantId") String tenantId);

    @SelectProvider(type = AiPromptTemplateSqlProvider.class, method = "selectByTenantIdAndPromptCodeAndVersion")
    Optional<AiPromptTemplate> selectByTenantIdAndPromptCodeAndVersion(@Param("tenantId") String tenantId,
                                                                       @Param("promptCode") String promptCode,
                                                                       @Param("version") String version);

    @InsertProvider(type = AiPromptTemplateSqlProvider.class, method = "upsert")
    void upsert(AiPromptTemplate template);
}
