/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.controller;

import java.util.List;

/**
 * 角色菜单绑定请求。
 */
public class RoleMenuBindRequest {

    /** 租户业务编码，用于限制角色菜单绑定的租户范围。 */
    private String tenantId;
    /** 菜单主键列表，包含目录、菜单、按钮或 API 权限节点。 */
    private List<Long> menuIds;

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 返回菜单主键列表。
     *
     * @return 菜单主键列表
     */
    public List<Long> getMenuIds() {
        return menuIds;
    }

    /**
     * 设置菜单主键列表。
     *
     * @param menuIds 菜单主键列表
     */
    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}
