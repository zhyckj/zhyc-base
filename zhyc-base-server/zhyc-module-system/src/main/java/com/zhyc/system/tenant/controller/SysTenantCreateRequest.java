/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.controller;

import com.zhyc.common.tenant.TenantIsolationMode;

import java.time.LocalDateTime;

/**
 * 系统租户创建请求。
 */
public class SysTenantCreateRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 租户名称。 */
    private String name;
    /** 当前租户套餐主键。 */
    private Long packageId;
    /** 租户隔离模式。 */
    private TenantIsolationMode isolationMode;
    /** 租户状态。 */
    private String status;
    /** 租户联系人姓名。 */
    private String contactName;
    /** 租户联系人电话。 */
    private String contactPhone;
    /** 租户到期时间。 */
    private LocalDateTime expireAt;

    /** @return 租户业务编码 */
    public String getTenantId() {
        return tenantId;
    }

    /** @param tenantId 租户业务编码 */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /** @return 租户名称 */
    public String getName() {
        return name;
    }

    /** @param name 租户名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 当前租户套餐主键 */
    public Long getPackageId() {
        return packageId;
    }

    /** @param packageId 当前租户套餐主键 */
    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    /** @return 租户隔离模式 */
    public TenantIsolationMode getIsolationMode() {
        return isolationMode;
    }

    /** @param isolationMode 租户隔离模式 */
    public void setIsolationMode(TenantIsolationMode isolationMode) {
        this.isolationMode = isolationMode;
    }

    /** @return 租户状态 */
    public String getStatus() {
        return status;
    }

    /** @param status 租户状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @return 租户联系人姓名 */
    public String getContactName() {
        return contactName;
    }

    /** @param contactName 租户联系人姓名 */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /** @return 租户联系人电话 */
    public String getContactPhone() {
        return contactPhone;
    }

    /** @param contactPhone 租户联系人电话 */
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    /** @return 租户到期时间 */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /** @param expireAt 租户到期时间 */
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
}
