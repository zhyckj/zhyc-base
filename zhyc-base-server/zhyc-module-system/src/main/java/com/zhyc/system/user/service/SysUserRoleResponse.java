/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

/**
 * 系统用户角色响应对象。
 */
public class SysUserRoleResponse {

    /** 角色主键。 */
    private final Long roleId;
    /** 角色编码。 */
    private final String roleCode;
    /** 角色名称。 */
    private final String roleName;
    /** 数据权限范围。 */
    private final String dataScope;
    /** 角色状态。 */
    private final String status;

    /**
     * 创建系统用户角色响应对象。
     *
     * @param roleId 角色主键
     * @param roleCode 角色编码
     * @param roleName 角色名称
     * @param dataScope 数据权限范围
     * @param status 角色状态
     */
    public SysUserRoleResponse(Long roleId, String roleCode, String roleName, String dataScope, String status) {
        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleName = roleName;
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
    public String getRoleName() {
        return roleName;
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
