/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.service;

import com.zhyc.system.menu.domain.SysMenu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统菜单树节点。
 */
public class SysMenuTreeNode implements Serializable {

    /** 序列化版本号，用于 Redis 缓存反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 数据库主键。 */
    private final Long id;
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
    /** 菜单状态；用于前端展示启用、停用等状态并驱动状态切换操作。 */
    private final String status;
    /** 子菜单节点。 */
    private final List<SysMenuTreeNode> children = new ArrayList<>();

    private SysMenuTreeNode(SysMenu menu) {
        this.id = menu.getId();
        this.parentId = menu.getParentId();
        this.menuCode = menu.getMenuCode();
        this.name = menu.getName();
        this.type = menu.getType();
        this.path = menu.getPath();
        this.component = menu.getComponent();
        this.permission = menu.getPermission();
        this.sortOrder = menu.getSortOrder();
        this.status = menu.getStatus();
    }

    /**
     * 从菜单领域对象创建树节点。
     *
     * @param menu 菜单领域对象
     * @return 菜单树节点
     */
    public static SysMenuTreeNode from(SysMenu menu) {
        return new SysMenuTreeNode(menu);
    }

    /**
     * 添加子菜单节点。
     *
     * @param child 子菜单节点
     */
    public void addChild(SysMenuTreeNode child) {
        children.add(child);
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
     * 返回父级菜单主键。
     *
     * @return 父级菜单主键
     */
    public Long getParentId() {
        return parentId;
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
     * 返回菜单名称。
     *
     * @return 菜单名称
     */
    public String getName() {
        return name;
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
     * 返回前端路由路径。
     *
     * @return 前端路由路径
     */
    public String getPath() {
        return path;
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
     * 返回权限标识。
     *
     * @return 权限标识
     */
    public String getPermission() {
        return permission;
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
     * 返回菜单状态。
     *
     * @return 菜单状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 返回子菜单节点。
     *
     * @return 子菜单节点
     */
    public List<SysMenuTreeNode> getChildren() {
        return children;
    }
}
