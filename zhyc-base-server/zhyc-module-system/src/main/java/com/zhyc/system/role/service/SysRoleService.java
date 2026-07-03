/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.service;

import java.util.List;

/**
 * 系统角色业务服务。
 */
public interface SysRoleService {

    /**
     * 查询租户内角色列表。
     *
     * @param tenantId 租户业务编码
     * @return 角色响应列表
     */
    List<SysRoleResponse> listRoles(String tenantId);

    /**
     * 新增或编辑系统角色。
     *
     * @param command 角色保存命令
     */
    void saveRole(SysRoleSaveCommand command);

    /**
     * 调整系统角色状态。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @param status 角色状态
     */
    void updateStatus(String tenantId, Long roleId, String status);

    /**
     * 删除系统角色。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     */
    void deleteRole(String tenantId, Long roleId);

    /**
     * 查询角色已绑定菜单权限。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @return 已绑定菜单主键列表
     */
    List<Long> listRoleMenuIds(String tenantId, Long roleId);

    /**
     * 绑定角色菜单权限。
     *
     * @param command 角色菜单绑定命令
     */
    void bindRoleMenus(RoleMenuBindCommand command);
}
