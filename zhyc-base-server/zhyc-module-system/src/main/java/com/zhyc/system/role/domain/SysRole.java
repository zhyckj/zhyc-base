/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.domain;

import com.zhyc.system.permission.DataScope;

import java.time.LocalDateTime;

/**
 * 系统角色基础模型。
 *
 * <p>角色数据按 {@code tenantId} 进行租户隔离，并包含数据权限范围字段。</p>
 */
public class SysRole {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码，用于限制角色所属租户。 */
    private String tenantId;
    /** 角色编码。 */
    private String roleCode;
    /** 角色名称。 */
    private String name;
    /** 数据权限范围。 */
    private DataScope dataScope;
    /** 角色状态，例如 enabled、disabled。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空角色对象。
     */
    public SysRole() {
    }

    /**
     * 创建完整角色对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param roleCode 角色编码
     * @param name 角色名称
     * @param dataScope 数据权限范围
     * @param status 角色状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysRole(Long id, String tenantId, String roleCode, String name, DataScope dataScope,
                   String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.roleCode = roleCode;
        this.name = name;
        this.dataScope = dataScope;
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
    public DataScope getDataScope() {
        return dataScope;
    }

    /**
     * 设置数据权限范围。
     *
     * @param dataScope 数据权限范围
     */
    public void setDataScope(DataScope dataScope) {
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
