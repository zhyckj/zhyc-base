/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.repository;

import com.zhyc.system.tenantpackage.domain.SysTenantPackage;

import java.util.List;
import java.util.Optional;

/**
 * 系统租户套餐仓储。
 */
public interface SysTenantPackageRepository {

    /**
     * 按状态查询租户套餐列表。
     *
     * @param status 套餐状态
     * @return 租户套餐列表
     */
    List<SysTenantPackage> findByStatus(String status);

    /**
     * 按套餐编码查询租户套餐。
     *
     * @param packageCode 套餐编码
     * @return 租户套餐
     */
    Optional<SysTenantPackage> findByCode(String packageCode);

    /**
     * 保存租户套餐。
     *
     * @param tenantPackage 待保存租户套餐
     * @return 保存后的租户套餐
     */
    SysTenantPackage save(SysTenantPackage tenantPackage);

    /**
     * 修改租户套餐状态。
     *
     * @param packageCode 套餐编码
     * @param status 目标状态
     */
    void updateStatus(String packageCode, String status);
}
