/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.domain;

import java.time.LocalDateTime;

/**
 * 租户套餐模块授权模型。
 */
public class SysTenantPackageModule {

    /** 数据库主键。 */
    private Long id;
    /** 租户套餐主键。 */
    private Long packageId;
    /** 模块编码。 */
    private String moduleCode;
    /** 菜单编码。 */
    private String menuCode;
    /** 权限标识。 */
    private String permission;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空租户套餐模块授权对象。
     */
    public SysTenantPackageModule() {
    }

    /**
     * 创建完整租户套餐模块授权对象。
     *
     * @param id 数据库主键
     * @param packageId 租户套餐主键
     * @param moduleCode 模块编码
     * @param menuCode 菜单编码
     * @param permission 权限标识
     * @param createdAt 创建时间
     */
    public SysTenantPackageModule(Long id, Long packageId, String moduleCode, String menuCode,
                                  String permission, LocalDateTime createdAt) {
        this.id = id;
        this.packageId = packageId;
        this.moduleCode = moduleCode;
        this.menuCode = menuCode;
        this.permission = permission;
        this.createdAt = createdAt;
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
     * 返回租户套餐主键。
     *
     * @return 租户套餐主键
     */
    public Long getPackageId() {
        return packageId;
    }

    /**
     * 设置租户套餐主键。
     *
     * @param packageId 租户套餐主键
     */
    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    /**
     * 返回模块编码。
     *
     * @return 模块编码
     */
    public String getModuleCode() {
        return moduleCode;
    }

    /**
     * 设置模块编码。
     *
     * @param moduleCode 模块编码
     */
    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
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
}
