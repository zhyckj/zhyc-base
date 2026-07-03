/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.domain;

import java.time.LocalDateTime;

/**
 * 系统角色自定义数据权限模型。
 *
 * <p>仅保存角色在 {@code CUSTOM} 数据权限范围下可访问的组织机构。</p>
 */
public class SysRoleDataScope {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 角色主键。 */
    private Long roleId;
    /** 授权组织主键。 */
    private Long orgId;
    /** 授权组织名称，仅查询展示时返回。 */
    private String orgName;
    /** 范围类型，首期固定为 org。 */
    private String scopeType;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空角色自定义数据权限对象。
     */
    public SysRoleDataScope() {
    }

    /**
     * 创建完整角色自定义数据权限对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @param orgId 授权组织主键
     * @param orgName 授权组织名称
     * @param scopeType 范围类型
     * @param createdAt 创建时间
     */
    public SysRoleDataScope(Long id, String tenantId, Long roleId, Long orgId, String orgName, String scopeType,
                            LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.roleId = roleId;
        this.orgId = orgId;
        this.orgName = orgName;
        this.scopeType = scopeType;
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
     * 返回授权组织主键。
     *
     * @return 授权组织主键
     */
    public Long getOrgId() {
        return orgId;
    }

    /**
     * 设置授权组织主键。
     *
     * @param orgId 授权组织主键
     */
    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    /**
     * 返回授权组织名称。
     *
     * @return 授权组织名称
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * 设置授权组织名称。
     *
     * @param orgName 授权组织名称
     */
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    /**
     * 返回范围类型。
     *
     * @return 范围类型
     */
    public String getScopeType() {
        return scopeType;
    }

    /**
     * 设置范围类型。
     *
     * @param scopeType 范围类型
     */
    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
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
