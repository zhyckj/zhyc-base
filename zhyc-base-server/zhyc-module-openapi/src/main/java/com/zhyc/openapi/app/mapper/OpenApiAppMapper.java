/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.mapper;

import com.zhyc.openapi.app.domain.OpenApiApp;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 开发者应用 MyBatis Mapper。
 */
@Mapper
public interface OpenApiAppMapper {

    /**
     * 查询租户开发者应用列表。
     *
     * @param tenantId 租户业务编码
     * @return 开发者应用列表
     */
    @SelectProvider(type = OpenApiAppSqlProvider.class, method = "selectByTenantId")
    List<OpenApiApp> selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按租户和应用编码查询开发者应用。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 匹配的开发者应用，不存在时返回 {@code null}
     */
    @SelectProvider(type = OpenApiAppSqlProvider.class, method = "selectByTenantIdAndAppCode")
    OpenApiApp selectByTenantIdAndAppCode(@Param("tenantId") String tenantId,
                                          @Param("appCode") String appCode);

    /**
     * 保存或更新开发者应用。
     *
     * @param app 开发者应用
     */
    @InsertProvider(type = OpenApiAppSqlProvider.class, method = "upsert")
    void upsert(OpenApiApp app);
}
