/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.repository;

import com.zhyc.system.role.domain.SysRole;

import java.util.List;

/**
 * 系统角色仓储。
 */
public interface SysRoleRepository {

    /**
     * 查询租户内角色列表。
     *
     * @param tenantId 租户业务编码
     * @return 角色列表
     */
    List<SysRole> findByTenantId(String tenantId);

    /**
     * 新增系统角色。
     *
     * @param role 系统角色
     */
    void insert(SysRole role);

    /**
     * 更新系统角色。
     *
     * @param role 系统角色
     */
    void update(SysRole role);

    /**
     * 更新系统角色状态。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @param status 角色状态
     */
    void updateStatus(String tenantId, Long roleId, String status);

    /**
     * 删除系统角色及其关联授权。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     */
    void deleteByTenantIdAndId(String tenantId, Long roleId);

    /**
     * 查询角色已绑定菜单权限。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @return 已绑定菜单主键列表
     */
    List<Long> findRoleMenuIds(String tenantId, Long roleId);

    /**
     * 重置角色菜单权限。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @param menuIds 菜单主键列表
     */
    void replaceRoleMenus(String tenantId, Long roleId, List<Long> menuIds);
}
