/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.service;

import java.util.List;

/**
 * 角色菜单绑定命令。
 */
public class RoleMenuBindCommand {

    /** 租户业务编码，用于限制角色和菜单必须属于同一租户。 */
    private final String tenantId;
    /** 角色主键。 */
    private final Long roleId;
    /** 菜单主键列表，包含目录、菜单、按钮或 API 权限节点。 */
    private final List<Long> menuIds;

    /**
     * 创建角色菜单绑定命令。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @param menuIds 菜单主键列表
     */
    public RoleMenuBindCommand(String tenantId, Long roleId, List<Long> menuIds) {
        this.tenantId = tenantId;
        this.roleId = roleId;
        this.menuIds = menuIds;
    }

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 返回角色主键。
     *
     * @return 角色主键
     */
    public Long getRoleId() {
        return roleId;
    }

    /**
     * 返回菜单主键列表。
     *
     * @return 菜单主键列表
     */
    public List<Long> getMenuIds() {
        return menuIds;
    }
}
