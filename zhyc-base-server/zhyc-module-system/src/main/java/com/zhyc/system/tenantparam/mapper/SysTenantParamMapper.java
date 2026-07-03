/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam.mapper;

import com.zhyc.system.tenantparam.domain.SysTenantParam;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 租户参数 MyBatis Mapper。
 */
@Mapper
public interface SysTenantParamMapper {

    /**
     * 查询租户参数列表。
     *
     * @param tenantId 租户业务编码
     * @return 租户参数列表
     */
    @SelectProvider(type = SysTenantParamSqlProvider.class, method = "selectByTenantId")
    List<SysTenantParam> selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按参数键查询租户参数。
     *
     * @param tenantId 租户业务编码
     * @param paramKey 参数键
     * @return 租户参数，不存在时为空
     */
    @SelectProvider(type = SysTenantParamSqlProvider.class, method = "selectByTenantIdAndParamKey")
    SysTenantParam selectByTenantIdAndParamKey(@Param("tenantId") String tenantId,
                                               @Param("paramKey") String paramKey);

    /**
     * 保存或更新租户参数。
     *
     * @param param 租户参数
     */
    @InsertProvider(type = SysTenantParamSqlProvider.class, method = "upsert")
    void upsert(SysTenantParam param);
}
