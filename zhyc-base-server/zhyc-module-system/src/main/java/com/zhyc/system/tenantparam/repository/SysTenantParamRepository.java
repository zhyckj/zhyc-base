/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam.repository;

import com.zhyc.system.tenantparam.domain.SysTenantParam;

import java.util.List;
import java.util.Optional;

/**
 * 租户参数仓储。
 */
public interface SysTenantParamRepository {

    /**
     * 查询租户参数列表。
     *
     * @param tenantId 租户业务编码
     * @return 租户参数列表
     */
    List<SysTenantParam> findByTenantId(String tenantId);

    /**
     * 按参数键查询租户参数。
     *
     * @param tenantId 租户业务编码
     * @param paramKey 参数键
     * @return 租户参数，不存在时为空
     */
    Optional<SysTenantParam> findByTenantIdAndParamKey(String tenantId, String paramKey);

    /**
     * 保存或更新租户参数。
     *
     * @param param 租户参数
     */
    void save(SysTenantParam param);
}
