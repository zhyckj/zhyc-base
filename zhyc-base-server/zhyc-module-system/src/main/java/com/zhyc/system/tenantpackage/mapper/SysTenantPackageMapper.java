/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.mapper;

import com.zhyc.system.tenantpackage.domain.SysTenantPackage;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统租户套餐 MyBatis Mapper。
 */
@Mapper
public interface SysTenantPackageMapper {

    /**
     * 按状态查询租户套餐列表。
     *
     * @param status 套餐状态
     * @return 租户套餐列表
     */
    @SelectProvider(type = SysTenantPackageSqlProvider.class, method = "selectByStatus")
    List<SysTenantPackage> selectByStatus(@Param("status") String status);

    /**
     * 按套餐编码查询租户套餐。
     *
     * @param packageCode 套餐编码
     * @return 租户套餐，不存在时返回 null
     */
    @SelectProvider(type = SysTenantPackageSqlProvider.class, method = "selectByCode")
    SysTenantPackage selectByCode(@Param("packageCode") String packageCode);

    /**
     * 新增租户套餐。
     *
     * @param tenantPackage 待新增租户套餐
     */
    @InsertProvider(type = SysTenantPackageSqlProvider.class, method = "insertPackage")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SysTenantPackage tenantPackage);

    /**
     * 修改租户套餐状态。
     *
     * @param packageCode 套餐编码
     * @param status 目标状态
     */
    @UpdateProvider(type = SysTenantPackageSqlProvider.class, method = "updateStatus")
    void updateStatus(@Param("packageCode") String packageCode, @Param("status") String status);
}
