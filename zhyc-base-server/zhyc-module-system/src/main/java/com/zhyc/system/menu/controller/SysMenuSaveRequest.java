/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.controller;

/**
 * 系统菜单保存请求。
 */
public class SysMenuSaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 父级菜单主键。 */
    private Long parentId;
    /** 菜单编码。 */
    private String menuCode;
    /** 菜单名称。 */
    private String name;
    /** 菜单类型。 */
    private String type;
    /** 前端路由路径。 */
    private String path;
    /** 前端组件路径。 */
    private String component;
    /** 权限标识。 */
    private String permission;
    /** 排序号。 */
    private Integer sortOrder;
    /** 菜单状态。 */
    private String status;

    public String getTenantId() { return tenantId; }

    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getParentId() { return parentId; }

    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getMenuCode() { return menuCode; }

    public void setMenuCode(String menuCode) { this.menuCode = menuCode; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getPath() { return path; }

    public void setPath(String path) { this.path = path; }

    public String getComponent() { return component; }

    public void setComponent(String component) { this.component = component; }

    public String getPermission() { return permission; }

    public void setPermission(String permission) { this.permission = permission; }

    public Integer getSortOrder() { return sortOrder; }

    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
