/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.service;

/**
 * 系统角色保存命令。
 */
public class SysRoleSaveCommand {

    /** 角色主键，新增时为空。 */
    private final Long roleId;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 角色编码。 */
    private final String roleCode;
    /** 角色名称。 */
    private final String name;
    /** 数据权限范围。 */
    private final String dataScope;
    /** 角色状态。 */
    private final String status;

    /**
     * 创建系统角色保存命令。
     *
     * @param roleId 角色主键，新增时为空
     * @param tenantId 租户业务编码
     * @param roleCode 角色编码
     * @param name 角色名称
     * @param dataScope 数据权限范围
     * @param status 角色状态
     */
    public SysRoleSaveCommand(Long roleId, String tenantId, String roleCode, String name, String dataScope,
                              String status) {
        this.roleId = roleId;
        this.tenantId = tenantId;
        this.roleCode = roleCode;
        this.name = name;
        this.dataScope = dataScope;
        this.status = status;
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
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 返回角色编码。
     *
     * @return 角色编码
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * 返回角色名称。
     *
     * @return 角色名称
     */
    public String getName() {
        return name;
    }

    /**
     * 返回数据权限范围。
     *
     * @return 数据权限范围
     */
    public String getDataScope() {
        return dataScope;
    }

    /**
     * 返回角色状态。
     *
     * @return 角色状态
     */
    public String getStatus() {
        return status;
    }
}
