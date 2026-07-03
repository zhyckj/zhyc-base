/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission.mapper;

import com.zhyc.openapi.permission.domain.OpenApiPermission;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 开放 API 权限授权 MyBatis Mapper。
 */
@Mapper
public interface OpenApiPermissionMapper {

    /**
     * 查询租户指定应用的开放 API 授权列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 开放 API 授权列表
     */
    @SelectProvider(type = OpenApiPermissionSqlProvider.class, method = "selectByTenantIdAndAppCode")
    List<OpenApiPermission> selectByTenantIdAndAppCode(@Param("tenantId") String tenantId,
                                                        @Param("appCode") String appCode);

    /**
     * 保存或更新开放 API 授权。
     *
     * @param permission 开放 API 权限授权领域对象
     */
    @InsertProvider(type = OpenApiPermissionSqlProvider.class, method = "upsert")
    void upsert(OpenApiPermission permission);
}
