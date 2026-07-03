/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.domain;

import java.time.LocalDateTime;

/**
 * 系统用户角色绑定模型。
 *
 * <p>用户角色绑定按 {@code tenantId} 做租户隔离，用于描述用户拥有的系统角色。</p>
 */
public class SysUserRole {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 用户主键。 */
    private Long userId;
    /** 角色主键。 */
    private Long roleId;
    /** 角色编码，仅查询展示时返回。 */
    private String roleCode;
    /** 角色名称，仅查询展示时返回。 */
    private String roleName;
    /** 数据权限范围，仅查询展示时返回。 */
    private String dataScope;
    /** 角色状态，仅查询展示时返回。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空用户角色绑定对象。
     */
    public SysUserRole() {
    }

    /**
     * 创建完整用户角色绑定对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param roleId 角色主键
     * @param roleCode 角色编码
     * @param roleName 角色名称
     * @param dataScope 数据权限范围
     * @param status 角色状态
     * @param createdAt 创建时间
     */
    public SysUserRole(Long id, String tenantId, Long userId, Long roleId, String roleCode, String roleName,
                       String dataScope, String status, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.dataScope = dataScope;
        this.status = status;
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
     * 返回用户主键。
     *
     * @return 用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户主键。
     *
     * @param userId 用户主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
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
     * 设置角色主键。
     *
     * @param roleId 角色主键
     */
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
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
    public String getRoleName() {
        return roleName;
    }

    /**
     * 设置角色名称。
     *
     * @param roleName 角色名称
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
