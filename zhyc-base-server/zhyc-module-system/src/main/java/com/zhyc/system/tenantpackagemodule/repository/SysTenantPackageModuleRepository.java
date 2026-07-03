/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.repository;

import com.zhyc.system.tenantpackagemodule.domain.SysTenantPackageModule;

import java.util.List;

/**
 * 租户套餐模块授权仓储。
 */
public interface SysTenantPackageModuleRepository {

    /**
     * 查询套餐授权资源列表。
     *
     * @param packageId 租户套餐主键
     * @return 套餐授权资源列表
     */
    List<SysTenantPackageModule> findByPackageId(Long packageId);

    /**
     * 删除指定套餐的全部授权资源。
     *
     * @param packageId 租户套餐主键
     */
    void deleteByPackageId(Long packageId);

    /**
     * 批量新增套餐授权资源。
     *
     * @param grants 套餐授权资源列表
     */
    void batchInsert(List<SysTenantPackageModule> grants);
}
