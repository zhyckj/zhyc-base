/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.domain;

/**
 * 系统租户套餐状态枚举。
 *
 * <p>状态编码持久化到 sys_tenant_package.status 字段，用于控制套餐是否可分配给租户。</p>
 */
public enum SysTenantPackageStatus {

    /** 启用状态，套餐可被租户订购或分配。 */
    ENABLED("enabled", "启用"),

    /** 禁用状态，套餐不可继续分配给新租户。 */
    DISABLED("disabled", "禁用");

    /** 持久化状态编码；对应租户套餐表 status 字段。 */
    private final String code;

    /** 状态中文说明；用于后台展示和审计说明。 */
    private final String description;

    /**
     * 创建系统租户套餐状态枚举。
     *
     * @param code 持久化状态编码
     * @param description 状态中文说明
     */
    SysTenantPackageStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取持久化状态编码。
     *
     * @return sys_tenant_package.status 字段使用的状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取状态中文说明。
     *
     * @return 状态中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析租户套餐状态。
     *
     * <p>只允许平台套餐生命周期支持的状态，避免非法状态进入 SaaS 套餐授权边界。</p>
     *
     * @param code 持久化状态编码
     * @return 匹配的租户套餐状态
     */
    public static SysTenantPackageStatus fromCode(String code) {
        for (SysTenantPackageStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("套餐状态不支持: " + code);
    }
}
