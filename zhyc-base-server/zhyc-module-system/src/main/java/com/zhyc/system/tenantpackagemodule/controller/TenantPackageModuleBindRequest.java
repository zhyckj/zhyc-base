/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.controller;

import java.util.List;

/**
 * 租户套餐模块授权绑定请求。
 */
public class TenantPackageModuleBindRequest {

    /** 授权项列表。 */
    private List<TenantPackageModuleGrantRequest> grants = List.of();

    /**
     * 返回授权项列表。
     *
     * @return 授权项列表
     */
    public List<TenantPackageModuleGrantRequest> getGrants() {
        return grants;
    }

    /**
     * 设置授权项列表。
     *
     * @param grants 授权项列表
     */
    public void setGrants(List<TenantPackageModuleGrantRequest> grants) {
        this.grants = grants == null ? List.of() : List.copyOf(grants);
    }
}
