/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.domain;

import java.time.LocalDateTime;

/**
 * 系统管理员管理范围模型。
 *
 * <p>用于限制租户管理员或平台管理员可以管理的租户、组织、模块等范围。</p>
 */
public class SysAdminScope {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 管理员用户主键。 */
    private Long userId;
    /** 范围类型，例如 tenant、org、module。 */
    private String scopeType;
    /** 范围引用编码，例如租户编码、组织主键字符串或模块编码。 */
    private String scopeRefCode;
    /** 范围展示名称，仅查询展示时返回。 */
    private String scopeName;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空管理员管理范围对象。
     */
    public SysAdminScope() {
    }

    /**
     * 创建完整管理员管理范围对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param userId 管理员用户主键
     * @param scopeType 范围类型
     * @param scopeRefCode 范围引用编码
     * @param scopeName 范围展示名称
     * @param createdAt 创建时间
     */
    public SysAdminScope(Long id, String tenantId, Long userId, String scopeType, String scopeRefCode,
                         String scopeName, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.scopeType = scopeType;
        this.scopeRefCode = scopeRefCode;
        this.scopeName = scopeName;
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
     * 返回管理员用户主键。
     *
     * @return 管理员用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置管理员用户主键。
     *
     * @param userId 管理员用户主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
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
     * 返回范围引用编码。
     *
     * @return 范围引用编码
     */
    public String getScopeRefCode() {
        return scopeRefCode;
    }

    /**
     * 设置范围引用编码。
     *
     * @param scopeRefCode 范围引用编码
     */
    public void setScopeRefCode(String scopeRefCode) {
        this.scopeRefCode = scopeRefCode;
    }

    /**
     * 返回范围展示名称。
     *
     * @return 范围展示名称
     */
    public String getScopeName() {
        return scopeName;
    }

    /**
     * 设置范围展示名称。
     *
     * @param scopeName 范围展示名称
     */
    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
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
