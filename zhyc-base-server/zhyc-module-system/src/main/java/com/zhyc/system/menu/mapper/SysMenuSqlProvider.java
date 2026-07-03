/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.mapper;

/**
 * 系统菜单 SQL Provider。
 */
public class SysMenuSqlProvider {

    /**
     * 生成租户内启用菜单查询 SQL。
     *
     * @return 菜单查询 SQL
     */
    public String selectEnabledByTenantId() {
        return selectBaseSql("AND status = 'enabled'");
    }

    public String selectByTenantId() {
        return selectBaseSql("");
    }

    private String selectBaseSql(String statusCondition) {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   parent_id AS parentId,
                   menu_code AS menuCode,
                   menu_name AS name,
                   menu_type AS type,
                   path,
                   component,
                   permission,
                   sort_order AS sortOrder,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_menu
            WHERE tenant_id = #{tenantId}
              %s
            ORDER BY COALESCE(parent_id, 0), sort_order, id
            """.formatted(statusCondition);
    }

    public String insert() {
        return """
            INSERT INTO sys_menu (
                tenant_id, parent_id, menu_code, menu_name, menu_type, path, component, permission, sort_order, status
            ) VALUES (
                #{tenantId}, #{parentId}, #{menuCode}, #{name}, #{type}, #{path}, #{component}, #{permission}, #{sortOrder}, #{status}
            )
            """;
    }

    public String update() {
        return """
            UPDATE sys_menu
            SET parent_id = #{parentId},
                menu_code = #{menuCode},
                menu_name = #{name},
                menu_type = #{type},
                path = #{path},
                component = #{component},
                permission = #{permission},
                sort_order = #{sortOrder},
                status = #{status}
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
            """;
    }

    public String updateStatus() {
        return """
            UPDATE sys_menu
            SET status = #{status}
            WHERE tenant_id = #{tenantId}
              AND id = #{menuId}
            """;
    }

    public String deleteRoleMenusByTenantAndMenu() {
        return """
            DELETE FROM sys_role_menu
            WHERE tenant_id = #{tenantId}
              AND menu_id = #{menuId}
            """;
    }

    public String deleteByTenantIdAndId() {
        return """
            DELETE FROM sys_menu
            WHERE tenant_id = #{tenantId}
              AND id = #{menuId}
            """;
    }
}
