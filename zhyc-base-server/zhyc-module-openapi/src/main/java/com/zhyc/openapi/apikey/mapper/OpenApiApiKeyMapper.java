/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.mapper;

import com.zhyc.openapi.apikey.domain.OpenApiApiKey;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * API Key MyBatis Mapper。
 */
@Mapper
public interface OpenApiApiKeyMapper {

    /**
     * 查询租户指定应用的 API Key 列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return API Key 列表
     */
    @SelectProvider(type = OpenApiApiKeySqlProvider.class, method = "selectByTenantIdAndAppCode")
    List<OpenApiApiKey> selectByTenantIdAndAppCode(@Param("tenantId") String tenantId,
                                                    @Param("appCode") String appCode);

    /**
     * 保存或更新 API Key。
     *
     * @param apiKey API Key 领域对象
     */
    @InsertProvider(type = OpenApiApiKeySqlProvider.class, method = "upsert")
    void upsert(OpenApiApiKey apiKey);
}
