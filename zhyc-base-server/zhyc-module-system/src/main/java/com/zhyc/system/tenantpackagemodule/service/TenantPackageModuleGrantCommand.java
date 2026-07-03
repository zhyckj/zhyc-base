/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.service;

/**
 * 租户套餐模块授权项命令。
 */
public class TenantPackageModuleGrantCommand {

    /** 模块编码。 */
    private final String moduleCode;
    /** 菜单编码。 */
    private final String menuCode;
    /** 权限标识。 */
    private final String permission;

    /**
     * 创建租户套餐模块授权项命令。
     *
     * @param moduleCode 模块编码
     * @param menuCode 菜单编码
     * @param permission 权限标识
     */
    public TenantPackageModuleGrantCommand(String moduleCode, String menuCode, String permission) {
        this.moduleCode = moduleCode;
        this.menuCode = menuCode;
        this.permission = permission;
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
     * 返回菜单编码。
     *
     * @return 菜单编码
     */
    public String getMenuCode() {
        return menuCode;
    }

    /**
     * 返回权限标识。
     *
     * @return 权限标识
     */
    public String getPermission() {
        return permission;
    }
}
