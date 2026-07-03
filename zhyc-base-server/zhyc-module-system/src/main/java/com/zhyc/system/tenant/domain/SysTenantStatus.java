/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.domain;

/**
 * 系统租户状态枚举。
 *
 * <p>状态编码持久化到 sys_tenant.status 字段，用于控制租户是否允许登录、访问和继续使用平台能力。</p>
 */
public enum SysTenantStatus {

    /** 启用状态，租户可正常访问平台。 */
    ENABLED("enabled", "启用"),

    /** 禁用状态，租户被人工停用后不可继续访问。 */
    DISABLED("disabled", "禁用"),

    /** 过期状态，租户超过有效期后不可继续访问。 */
    EXPIRED("expired", "已过期");

    /** 持久化状态编码；对应 sys_tenant.status 字段。 */
    private final String code;

    /** 状态中文说明；用于后台展示和审计说明。 */
    private final String description;

    /**
     * 创建系统租户状态枚举。
     *
     * @param code 持久化状态编码
     * @param description 状态中文说明
     */
    SysTenantStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取持久化状态编码。
     *
     * @return sys_tenant.status 字段使用的状态编码
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
     * 根据持久化编码解析租户状态。
     *
     * <p>只允许设计表结构声明的租户状态，避免非法状态进入租户启停和查询条件。</p>
     *
     * @param code 持久化状态编码
     * @return 匹配的租户状态枚举
     */
    public static SysTenantStatus fromCode(String code) {
        for (SysTenantStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("租户状态不支持: " + code);
    }
}
