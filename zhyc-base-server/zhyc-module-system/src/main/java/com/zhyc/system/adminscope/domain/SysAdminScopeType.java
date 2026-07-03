/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.domain;

/**
 * 系统管理员管理范围类型枚举。
 *
 * <p>类型编码持久化到 sys_admin_scope.scope_type 字段，用于限制管理员可管理的租户、组织和模块范围。</p>
 */
public enum SysAdminScopeType {

    /** 租户范围，用于授予管理员管理指定租户的能力。 */
    TENANT("tenant", "租户"),

    /** 组织范围，用于授予管理员管理指定组织及其业务数据的能力。 */
    ORG("org", "组织"),

    /** 模块范围，用于授予管理员管理指定平台模块的能力。 */
    MODULE("module", "模块");

    /** 持久化类型编码；对应管理员管理范围表 scope_type 字段。 */
    private final String code;

    /** 类型中文说明；用于后台展示和审计说明。 */
    private final String description;

    /**
     * 创建系统管理员管理范围类型枚举。
     *
     * @param code 持久化类型编码
     * @param description 类型中文说明
     */
    SysAdminScopeType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取持久化类型编码。
     *
     * @return 管理员管理范围表 scope_type 字段使用的类型编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取类型中文说明。
     *
     * @return 类型中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析管理员管理范围类型。
     *
     * <p>只允许平台内置的管理范围维度，避免非法范围类型进入权限边界。</p>
     *
     * @param code 持久化类型编码
     * @return 匹配的管理员管理范围类型
     */
    public static SysAdminScopeType fromCode(String code) {
        for (SysAdminScopeType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("管理员管理范围类型不支持: " + code);
    }
}
