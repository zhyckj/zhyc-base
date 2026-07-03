/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.mapper;

import com.zhyc.system.tenantpackagemodule.domain.SysTenantPackageModule;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 租户套餐模块授权 MyBatis Mapper。
 */
@Mapper
public interface SysTenantPackageModuleMapper {

    /**
     * 查询套餐授权资源列表。
     *
     * @param packageId 租户套餐主键
     * @return 套餐授权资源列表
     */
    @SelectProvider(type = SysTenantPackageModuleSqlProvider.class, method = "selectByPackageId")
    List<SysTenantPackageModule> selectByPackageId(@Param("packageId") Long packageId);

    /**
     * 删除指定套餐的全部授权资源。
     *
     * @param packageId 租户套餐主键
     */
    @DeleteProvider(type = SysTenantPackageModuleSqlProvider.class, method = "deleteByPackageId")
    void deleteByPackageId(@Param("packageId") Long packageId);

    /**
     * 新增套餐授权资源。
     *
     * @param grant 套餐授权资源
     */
    @InsertProvider(type = SysTenantPackageModuleSqlProvider.class, method = "insert")
    void insert(SysTenantPackageModule grant);
}
