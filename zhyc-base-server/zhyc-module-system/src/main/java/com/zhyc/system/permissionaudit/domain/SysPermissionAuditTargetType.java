/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit.domain;

/**
 * 系统权限变更审计目标类型枚举。
 *
 * <p>类型编码持久化到 sys_permission_audit.target_type 字段，用于限定权限变更审计指向的业务对象。</p>
 */
public enum SysPermissionAuditTargetType {

    /** 角色目标，用于记录角色菜单权限或数据权限调整。 */
    ROLE("role", "角色"),

    /** 用户目标，用于记录用户角色授权或用户权限调整。 */
    USER("user", "用户"),

    /** 菜单目标，用于记录菜单或按钮权限配置调整。 */
    MENU("menu", "菜单");

    /** 持久化类型编码；对应权限变更审计表 target_type 字段。 */
    private final String code;

    /** 类型中文说明；用于后台展示和审计说明。 */
    private final String description;

    /**
     * 创建系统权限变更审计目标类型枚举。
     *
     * @param code 持久化类型编码
     * @param description 类型中文说明
     */
    SysPermissionAuditTargetType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取持久化类型编码。
     *
     * @return sys_permission_audit.target_type 字段使用的类型编码
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
     * 根据持久化编码解析权限变更审计目标类型。
     *
     * <p>只允许权限审计支持的目标对象类型，避免未定义目标污染审计数据。</p>
     *
     * @param code 持久化类型编码
     * @return 匹配的权限变更审计目标类型
     */
    public static SysPermissionAuditTargetType fromCode(String code) {
        for (SysPermissionAuditTargetType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("权限审计目标类型不支持: " + code);
    }
}
