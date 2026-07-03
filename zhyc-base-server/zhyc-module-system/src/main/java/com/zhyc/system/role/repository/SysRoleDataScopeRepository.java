/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.repository;

import com.zhyc.system.role.domain.SysRoleDataScope;

import java.util.List;

/**
 * 系统角色自定义数据权限仓储接口。
 */
public interface SysRoleDataScopeRepository {

    /**
     * 查询租户内角色自定义组织范围。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @return 角色自定义数据权限列表
     */
    List<SysRoleDataScope> findByTenantIdAndRoleId(String tenantId, Long roleId);

    /**
     * 替换租户内角色自定义组织范围。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @param orgIds 授权组织主键列表
     */
    void replaceRoleDataScopes(String tenantId, Long roleId, List<Long> orgIds);
}
