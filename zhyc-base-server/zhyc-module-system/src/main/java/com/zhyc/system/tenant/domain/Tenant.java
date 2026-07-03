/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.domain;

import com.zhyc.common.tenant.TenantIsolationMode;

import java.time.LocalDateTime;

/**
 * 租户基础信息模型。
 *
 * <p>用于描述平台租户主数据，默认采用租户字段隔离模式。</p>
 */
public class Tenant {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码，用于跨表租户数据隔离。 */
    private String tenantId;
    /** 租户名称。 */
    private String name;
    /** 当前租户套餐主键。 */
    private Long packageId;
    /** 租户隔离模式，默认使用租户字段隔离。 */
    private TenantIsolationMode isolationMode = TenantIsolationMode.TENANT_COLUMN;
    /** 租户状态，例如 enabled、disabled。 */
    private String status;
    /** 租户联系人姓名。 */
    private String contactName;
    /** 租户联系人电话。 */
    private String contactPhone;
    /** 租户到期时间。 */
    private LocalDateTime expireAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空租户对象，隔离模式默认使用 {@link TenantIsolationMode#TENANT_COLUMN}。
     */
    public Tenant() {
    }

    /**
     * 创建完整租户对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param name 租户名称
     * @param isolationMode 租户隔离模式；传入 {@code null} 时使用默认租户字段隔离
     * @param status 租户状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public Tenant(Long id, String tenantId, String name, TenantIsolationMode isolationMode,
                  String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.isolationMode = isolationMode == null ? TenantIsolationMode.TENANT_COLUMN : isolationMode;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 创建包含套餐和联系人信息的完整租户对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param name 租户名称
     * @param packageId 当前租户套餐主键
     * @param isolationMode 租户隔离模式；传入 {@code null} 时使用默认租户字段隔离
     * @param status 租户状态
     * @param contactName 租户联系人姓名
     * @param contactPhone 租户联系人电话
     * @param expireAt 租户到期时间
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public Tenant(Long id, String tenantId, String name, Long packageId, TenantIsolationMode isolationMode,
                  String status, String contactName, String contactPhone, LocalDateTime expireAt,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.packageId = packageId;
        this.isolationMode = isolationMode == null ? TenantIsolationMode.TENANT_COLUMN : isolationMode;
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
     * 返回租户名称。
     *
     * @return 租户名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置租户名称。
     *
     * @param name 租户名称
     */
    public void setName(String name) {
        this.name = name;
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
     * 设置当前租户套餐主键。
     *
     * @param packageId 当前租户套餐主键
     */
    public void setPackageId(Long packageId) {
        this.packageId = packageId;
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
     * 设置租户隔离模式。
     *
     * @param isolationMode 租户隔离模式；传入 {@code null} 时使用默认租户字段隔离
     */
    public void setIsolationMode(TenantIsolationMode isolationMode) {
        this.isolationMode = isolationMode == null ? TenantIsolationMode.TENANT_COLUMN : isolationMode;
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
     * 设置租户状态。
     *
     * @param status 租户状态
     */
    public void setStatus(String status) {
        this.status = status;
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
     * 设置租户联系人姓名。
     *
     * @param contactName 租户联系人姓名
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
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
     * 设置租户联系人电话。
     *
     * @param contactPhone 租户联系人电话
     */
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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
     * 设置租户到期时间。
     *
     * @param expireAt 租户到期时间
     */
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
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
