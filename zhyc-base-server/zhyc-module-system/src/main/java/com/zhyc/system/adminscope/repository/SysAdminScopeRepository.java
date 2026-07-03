/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.repository;

import com.zhyc.system.adminscope.domain.SysAdminScope;

import java.util.List;

/**
 * 系统管理员管理范围仓储。
 */
public interface SysAdminScopeRepository {

    /**
     * 查询租户内管理员管理范围。
     *
     * @param tenantId 租户业务编码
     * @param userId 管理员用户主键
     * @return 管理员管理范围列表
     */
    List<SysAdminScope> findByTenantIdAndUserId(String tenantId, Long userId);

    /**
     * 替换租户内管理员管理范围。
     *
     * @param tenantId 租户业务编码
     * @param userId 管理员用户主键
     * @param scopes 管理员管理范围列表
     */
    void replaceAdminScopes(String tenantId, Long userId, List<SysAdminScope> scopes);
}
