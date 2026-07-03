/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.service;

/**
 * 系统菜单保存命令。
 */
public class SysMenuSaveCommand {

    /** 菜单主键，新增时为空。 */
    private final Long menuId;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 父级菜单主键。 */
    private final Long parentId;
    /** 菜单编码。 */
    private final String menuCode;
    /** 菜单名称。 */
    private final String name;
    /** 菜单类型。 */
    private final String type;
    /** 前端路由路径。 */
    private final String path;
    /** 前端组件路径。 */
    private final String component;
    /** 权限标识。 */
    private final String permission;
    /** 排序号。 */
    private final Integer sortOrder;
    /** 菜单状态。 */
    private final String status;

    public SysMenuSaveCommand(Long menuId, String tenantId, Long parentId, String menuCode, String name, String type,
                              String path, String component, String permission, Integer sortOrder, String status) {
        this.menuId = menuId;
        this.tenantId = tenantId;
        this.parentId = parentId;
        this.menuCode = menuCode;
        this.name = name;
        this.type = type;
        this.path = path;
        this.component = component;
        this.permission = permission;
        this.sortOrder = sortOrder;
        this.status = status;
    }

    public Long getMenuId() { return menuId; }

    public String getTenantId() { return tenantId; }

    public Long getParentId() { return parentId; }

    public String getMenuCode() { return menuCode; }

    public String getName() { return name; }

    public String getType() { return type; }

    public String getPath() { return path; }

    public String getComponent() { return component; }

    public String getPermission() { return permission; }

    public Integer getSortOrder() { return sortOrder; }

    public String getStatus() { return status; }
}
