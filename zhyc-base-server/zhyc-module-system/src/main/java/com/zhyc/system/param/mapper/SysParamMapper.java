/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.param.mapper;

import com.zhyc.system.param.domain.SysParam;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 系统参数 MyBatis Mapper。
 */
@Mapper
public interface SysParamMapper {

    /**
     * 查询租户系统参数列表。
     *
     * @param tenantId 租户业务编码
     * @return 系统参数列表
     */
    @SelectProvider(type = SysParamSqlProvider.class, method = "selectByTenantId")
    List<SysParam> selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按参数键查询租户系统参数。
     *
     * @param tenantId 租户业务编码
     * @param paramKey 参数键
     * @return 系统参数，不存在时为空
     */
    @SelectProvider(type = SysParamSqlProvider.class, method = "selectByTenantIdAndParamKey")
    SysParam selectByTenantIdAndParamKey(@Param("tenantId") String tenantId, @Param("paramKey") String paramKey);

    /**
     * 保存或更新租户系统参数。
     *
     * @param param 系统参数
     */
    @InsertProvider(type = SysParamSqlProvider.class, method = "upsert")
    void upsert(SysParam param);
}
