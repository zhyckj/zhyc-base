/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.repository;

import com.zhyc.system.user.domain.SysUserRole;

import java.util.List;

/**
 * 系统用户角色仓储接口。
 */
public interface SysUserRoleRepository {

    /**
     * 查询租户内指定用户的角色绑定。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @return 用户角色绑定列表
     */
    List<SysUserRole> findByTenantIdAndUserId(String tenantId, Long userId);

    /**
     * 替换租户内指定用户的角色绑定。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param bindings 角色绑定列表
     */
    void replaceUserRoles(String tenantId, Long userId, List<SysUserRole> bindings);
}
