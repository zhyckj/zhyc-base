/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.controller;

/**
 * 租户套餐模块授权项请求。
 */
public class TenantPackageModuleGrantRequest {

    /** 模块编码。 */
    private String moduleCode;
    /** 菜单编码。 */
    private String menuCode;
    /** 权限标识。 */
    private String permission;

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
}
