/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.repository;

import com.zhyc.system.tenantpackagemodule.domain.SysTenantPackageModule;
import com.zhyc.system.tenantpackagemodule.mapper.SysTenantPackageModuleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的租户套餐模块授权仓储实现。
 */
@Repository
public class MyBatisSysTenantPackageModuleRepository implements SysTenantPackageModuleRepository {

    /** 租户套餐模块授权 Mapper。 */
    private final SysTenantPackageModuleMapper moduleMapper;

    /**
     * 创建租户套餐模块授权仓储实现。
     *
     * @param moduleMapper 租户套餐模块授权 Mapper
     */
    public MyBatisSysTenantPackageModuleRepository(SysTenantPackageModuleMapper moduleMapper) {
        this.moduleMapper = Objects.requireNonNull(moduleMapper, "租户套餐模块授权 Mapper 不能为空");
    }

    @Override
    public List<SysTenantPackageModule> findByPackageId(Long packageId) {
        return moduleMapper.selectByPackageId(packageId);
    }

    @Override
    public void deleteByPackageId(Long packageId) {
        moduleMapper.deleteByPackageId(packageId);
    }

    @Override
    public void batchInsert(List<SysTenantPackageModule> grants) {
        for (SysTenantPackageModule grant : grants) {
            moduleMapper.insert(grant);
        }
    }
}
