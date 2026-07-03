/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.mapper;

import com.zhyc.ai.model.domain.AiModelConfig;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Optional;

/**
 * AI 模型配置 MyBatis Mapper。
 */
@Mapper
public interface AiModelConfigMapper {

    @SelectProvider(type = AiModelConfigSqlProvider.class, method = "selectByTenantId")
    List<AiModelConfig> selectByTenantId(@Param("tenantId") String tenantId);

    @SelectProvider(type = AiModelConfigSqlProvider.class, method = "selectByTenantIdAndModelCode")
    Optional<AiModelConfig> selectByTenantIdAndModelCode(@Param("tenantId") String tenantId,
                                                         @Param("modelCode") String modelCode);

    @SelectProvider(type = AiModelConfigSqlProvider.class, method = "selectByTenantIdAndId")
    Optional<AiModelConfig> selectByTenantIdAndId(@Param("tenantId") String tenantId, @Param("id") Long id);

    @InsertProvider(type = AiModelConfigSqlProvider.class, method = "upsert")
    void upsert(AiModelConfig modelConfig);
}
