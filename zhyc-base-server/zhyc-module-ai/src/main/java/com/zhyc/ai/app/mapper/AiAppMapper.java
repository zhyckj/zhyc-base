/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.mapper;

import com.zhyc.ai.app.domain.AiApp;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Optional;

/**
 * AI 应用接入 MyBatis Mapper。
 */
@Mapper
public interface AiAppMapper {

    @SelectProvider(type = AiAppSqlProvider.class, method = "selectByTenantId")
    List<AiApp> selectByTenantId(@Param("tenantId") String tenantId);

    @SelectProvider(type = AiAppSqlProvider.class, method = "selectByTenantIdAndAppCode")
    Optional<AiApp> selectByTenantIdAndAppCode(@Param("tenantId") String tenantId,
                                               @Param("appCode") String appCode);

    @InsertProvider(type = AiAppSqlProvider.class, method = "upsert")
    void upsert(AiApp app);
}
