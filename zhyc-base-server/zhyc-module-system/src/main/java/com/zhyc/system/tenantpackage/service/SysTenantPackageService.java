/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.service;

import java.util.List;

/**
 * 系统租户套餐业务服务。
 */
public interface SysTenantPackageService {

    /**
     * 按状态查询租户套餐列表。
     *
     * @param status 套餐状态
     * @return 租户套餐列表
     */
    List<SysTenantPackageResponse> listPackages(String status);

    /**
     * 创建租户套餐。
     *
     * @param command 租户套餐创建命令
     * @return 创建后的租户套餐
     */
    SysTenantPackageResponse createPackage(TenantPackageCreateCommand command);

    /**
     * 修改租户套餐状态。
     *
     * @param packageCode 套餐编码
     * @param status 目标状态
     */
    void changeStatus(String packageCode, String status);
}
