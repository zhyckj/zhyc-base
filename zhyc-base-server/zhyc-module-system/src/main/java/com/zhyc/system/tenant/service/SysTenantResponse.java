/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.service;

import com.zhyc.common.tenant.TenantIsolationMode;

import java.time.LocalDateTime;

/**
 * 系统租户响应对象。
 */
public class SysTenantResponse {

    /** 数据库主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 租户名称。 */
    private final String name;
    /** 当前租户套餐主键。 */
    private final Long packageId;
    /** 租户隔离模式。 */
    private final TenantIsolationMode isolationMode;
    /** 租户状态。 */
    private final String status;
    /** 租户联系人姓名。 */
    private final String contactName;
    /** 租户联系人电话。 */
    private final String contactPhone;
    /** 租户到期时间。 */
    private final LocalDateTime expireAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    /**
     * 创建系统租户响应对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param name 租户名称
     * @param packageId 当前租户套餐主键
     * @param isolationMode 租户隔离模式
     * @param status 租户状态
     * @param contactName 租户联系人姓名
     * @param contactPhone 租户联系人电话
     * @param expireAt 租户到期时间
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysTenantResponse(Long id, String tenantId, String name, Long packageId,
                             TenantIsolationMode isolationMode, String status, String contactName,
                             String contactPhone, LocalDateTime expireAt, LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.packageId = packageId;
        this.isolationMode = isolationMode;
        this.status = status;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.expireAt = expireAt;
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
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 返回租户名称。
     *
     * @return 租户名称
     */
    public String getName() {
        return name;
    }

    /**
     * 返回当前租户套餐主键。
     *
     * @return 当前租户套餐主键
     */
    public Long getPackageId() {
        return packageId;
    }

    /**
     * 返回租户隔离模式。
     *
     * @return 租户隔离模式
     */
    public TenantIsolationMode getIsolationMode() {
        return isolationMode;
    }

    /**
     * 返回租户状态。
     *
     * @return 租户状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 返回租户联系人姓名。
     *
     * @return 租户联系人姓名
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * 返回租户联系人电话。
     *
     * @return 租户联系人电话
     */
    public String getContactPhone() {
        return contactPhone;
    }

    /**
     * 返回租户到期时间。
     *
     * @return 租户到期时间
     */
    public LocalDateTime getExpireAt() {
        return expireAt;
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
     * 返回更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
