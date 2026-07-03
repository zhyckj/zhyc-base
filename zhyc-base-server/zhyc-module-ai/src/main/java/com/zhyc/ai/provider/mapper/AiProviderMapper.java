/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.mapper;

import com.zhyc.ai.provider.domain.AiProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Optional;

/**
 * AI 模型供应商 MyBatis Mapper。
 */
@Mapper
public interface AiProviderMapper {

    @SelectProvider(type = AiProviderSqlProvider.class, method = "selectByTenantId")
    List<AiProvider> selectByTenantId(@Param("tenantId") String tenantId);

    @SelectProvider(type = AiProviderSqlProvider.class, method = "selectByTenantIdAndProviderCode")
    Optional<AiProvider> selectByTenantIdAndProviderCode(@Param("tenantId") String tenantId,
                                                         @Param("providerCode") String providerCode);

    @SelectProvider(type = AiProviderSqlProvider.class, method = "selectByTenantIdAndId")
    Optional<AiProvider> selectByTenantIdAndId(@Param("tenantId") String tenantId, @Param("id") Long id);

    @InsertProvider(type = AiProviderSqlProvider.class, method = "upsert")
    void upsert(AiProvider provider);
}
