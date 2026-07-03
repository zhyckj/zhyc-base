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
 * 系统租户创建命令。
 */
public class SysTenantCreateCommand {

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

    /**
     * 创建系统租户创建命令。
     *
     * @param tenantId 租户业务编码
     * @param name 租户名称
     * @param packageId 当前租户套餐主键
     * @param isolationMode 租户隔离模式
     * @param status 租户状态
     * @param contactName 租户联系人姓名
     * @param contactPhone 租户联系人电话
     * @param expireAt 租户到期时间
     */
    public SysTenantCreateCommand(String tenantId, String name, Long packageId,
                                  TenantIsolationMode isolationMode, String status,
                                  String contactName, String contactPhone, LocalDateTime expireAt) {
        this.tenantId = tenantId;
        this.name = name;
        this.packageId = packageId;
        this.isolationMode = isolationMode;
        this.status = status;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.expireAt = expireAt;
    }

    /** @return 租户业务编码 */
    public String getTenantId() {
        return tenantId;
    }

    /** @return 租户名称 */
    public String getName() {
        return name;
    }

    /** @return 当前租户套餐主键 */
    public Long getPackageId() {
        return packageId;
    }

    /** @return 租户隔离模式 */
    public TenantIsolationMode getIsolationMode() {
        return isolationMode;
    }

    /** @return 租户状态 */
    public String getStatus() {
        return status;
    }

    /** @return 租户联系人姓名 */
    public String getContactName() {
        return contactName;
    }

    /** @return 租户联系人电话 */
    public String getContactPhone() {
        return contactPhone;
    }

    /** @return 租户到期时间 */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }
}
