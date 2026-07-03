/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit.domain;

/**
 * 系统权限审计变更类型枚举。
 *
 * <p>变更类型编码持久化到 sys_permission_audit.change_type 字段，用于区分角色、菜单和数据权限调整动作。</p>
 */
public enum SysPermissionAuditChangeType {

    /** 绑定菜单权限，用于记录角色或用户新增菜单、按钮权限。 */
    BIND_MENU("BIND_MENU", "绑定菜单权限"),

    /** 解绑菜单权限，用于记录角色或用户移除菜单、按钮权限。 */
    UNBIND_MENU("UNBIND_MENU", "解绑菜单权限"),

    /** 绑定角色权限，用于记录用户新增角色授权。 */
    BIND_ROLE("BIND_ROLE", "绑定角色权限"),

    /** 解绑角色权限，用于记录用户移除角色授权。 */
    UNBIND_ROLE("UNBIND_ROLE", "解绑角色权限"),

    /** 绑定数据权限，用于记录角色新增组织、部门或自定义数据范围。 */
    BIND_DATA_SCOPE("BIND_DATA_SCOPE", "绑定数据权限"),

    /** 更新数据权限，用于记录角色调整已有数据范围。 */
    UPDATE_DATA_SCOPE("UPDATE_DATA_SCOPE", "更新数据权限"),

    /** 清空数据权限，用于记录角色移除全部自定义数据范围。 */
    CLEAR_DATA_SCOPE("CLEAR_DATA_SCOPE", "清空数据权限");

    /** 持久化变更类型编码；对应权限审计表 change_type 字段。 */
    private final String code;

    /** 变更类型中文说明；用于后台展示和审计解释。 */
    private final String description;

    /**
     * 创建系统权限审计变更类型枚举。
     *
     * @param code 持久化变更类型编码
     * @param description 变更类型中文说明
     */
    SysPermissionAuditChangeType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取持久化变更类型编码。
     *
     * @return sys_permission_audit.change_type 字段使用的变更类型编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取变更类型中文说明。
     *
     * @return 变更类型中文说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据持久化编码解析权限审计变更类型。
     *
     * <p>只允许权限审计支持的变更动作，避免未定义动作污染审计统计和等保追溯数据。</p>
     *
     * @param code 持久化变更类型编码
     * @return 匹配的权限审计变更类型枚举
     */
    public static SysPermissionAuditChangeType fromCode(String code) {
        for (SysPermissionAuditChangeType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("权限审计变更类型不支持: " + code);
    }
}
