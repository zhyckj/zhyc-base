/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.domain;

import java.time.LocalDateTime;

/**
 * 系统菜单基础模型。
 *
 * <p>菜单数据按 {@code tenantId} 进行租户隔离，用于描述菜单、按钮或权限节点。</p>
 */
public class SysMenu {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码，用于限制菜单所属租户。 */
    private String tenantId;
    /** 父级菜单主键，根节点可为空。 */
    private Long parentId;
    /** 菜单编码。 */
    private String menuCode;
    /** 菜单名称。 */
    private String name;
    /** 菜单类型，例如 directory、menu、button。 */
    private String type;
    /** 前端路由路径。 */
    private String path;
    /** 前端组件路径。 */
    private String component;
    /** 权限标识。 */
    private String permission;
    /** 排序号，数值越小越靠前。 */
    private Integer sortOrder;
    /** 菜单状态，例如 enabled、disabled。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空菜单对象。
     */
    public SysMenu() {
    }

    /**
     * 创建完整菜单对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param parentId 父级菜单主键
     * @param menuCode 菜单编码
     * @param name 菜单名称
     * @param type 菜单类型
     * @param path 前端路由路径
     * @param component 前端组件路径
     * @param permission 权限标识
     * @param sortOrder 排序号
     * @param status 菜单状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysMenu(Long id, String tenantId, Long parentId, String menuCode, String name, String type,
                   String path, String component, String permission, Integer sortOrder, String status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 返回数据库主键。
     *
     * @return 数据库主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置数据库主键。
     *
     * @param id 数据库主键
     */
    public void setId(Long id) {
        this.id = id;
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
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 返回父级菜单主键。
     *
     * @return 父级菜单主键
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * 设置父级菜单主键。
     *
     * @param parentId 父级菜单主键
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * 返回菜单编码。
     *
     * @return 菜单编码
     */
    public String getMenuCode() {
        return menuCode;
    }

    /**
     * 设置菜单编码。
     *
     * @param menuCode 菜单编码
     */
    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }

    /**
     * 返回菜单名称。
     *
     * @return 菜单名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置菜单名称。
     *
     * @param name 菜单名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回菜单类型。
     *
     * @return 菜单类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置菜单类型。
     *
     * @param type 菜单类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 返回前端路由路径。
     *
     * @return 前端路由路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置前端路由路径。
     *
     * @param path 前端路由路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 返回前端组件路径。
     *
     * @return 前端组件路径
     */
    public String getComponent() {
        return component;
    }

    /**
     * 设置前端组件路径。
     *
     * @param component 前端组件路径
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * 返回权限标识。
     *
     * @return 权限标识
     */
    public String getPermission() {
        return permission;
    }

    /**
     * 设置权限标识。
     *
     * @param permission 权限标识
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * 返回排序号。
     *
     * @return 排序号
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 设置排序号。
     *
     * @param sortOrder 排序号
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 返回菜单状态。
     *
     * @return 菜单状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置菜单状态。
     *
     * @param status 菜单状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 返回更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间。
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
