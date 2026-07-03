/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.service;

import java.util.List;

/**
 * 租户套餐模块授权业务服务。
 */
public interface SysTenantPackageModuleService {

    /**
     * 查询套餐授权资源列表。
     *
     * @param packageId 租户套餐主键
     * @return 套餐授权资源列表
     */
    List<TenantPackageModuleResponse> listGrants(Long packageId);

    /**
     * 绑定套餐授权资源。
     *
     * @param command 套餐授权绑定命令
     */
    void bindGrants(TenantPackageModuleBindCommand command);
}
