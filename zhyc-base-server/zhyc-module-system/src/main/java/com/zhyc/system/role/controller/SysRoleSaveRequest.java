/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.controller;

/**
 * 系统角色保存请求。
 */
public class SysRoleSaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 角色编码。 */
    private String roleCode;
    /** 角色名称。 */
    private String name;
    /** 数据权限范围。 */
    private String dataScope;
    /** 角色状态，enabled 或 disabled。 */
    private String status;

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
     * 返回角色编码。
     *
     * @return 角色编码
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * 设置角色编码。
     *
     * @param roleCode 角色编码
     */
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
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
     * 设置角色名称。
     *
     * @param name 角色名称
     */
    public void setName(String name) {
        this.name = name;
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
     * 设置数据权限范围。
     *
     * @param dataScope 数据权限范围
     */
    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    /**
     * 返回角色状态。
     *
     * @return 角色状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置角色状态。
     *
     * @param status 角色状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
