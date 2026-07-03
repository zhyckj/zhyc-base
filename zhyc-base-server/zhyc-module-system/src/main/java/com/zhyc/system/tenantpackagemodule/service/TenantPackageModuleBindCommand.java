/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.service;

import java.util.List;

/**
 * 租户套餐模块授权绑定命令。
 */
public class TenantPackageModuleBindCommand {

    /** 租户套餐主键。 */
    private final Long packageId;
    /** 授权项列表。 */
    private final List<TenantPackageModuleGrantCommand> grants;

    /**
     * 创建租户套餐模块授权绑定命令。
     *
     * @param packageId 租户套餐主键
     * @param grants 授权项列表
     */
    public TenantPackageModuleBindCommand(Long packageId, List<TenantPackageModuleGrantCommand> grants) {
        this.packageId = packageId;
        this.grants = grants == null ? List.of() : List.copyOf(grants);
    }

    /**
     * 返回租户套餐主键。
     *
     * @return 租户套餐主键
     */
    public Long getPackageId() {
        return packageId;
    }

    /**
     * 返回授权项列表。
     *
     * @return 授权项列表
     */
    public List<TenantPackageModuleGrantCommand> getGrants() {
        return grants;
    }
}
