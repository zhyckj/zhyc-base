/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.mapper;

/**
 * 系统角色 SQL Provider。
 */
public class SysRoleSqlProvider {

    /**
     * 生成租户内角色列表查询 SQL。
     *
     * @return 角色列表查询 SQL
     */
    public String selectByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   role_code AS roleCode,
                   name,
                   data_scope AS dataScope,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_role
            WHERE tenant_id = #{tenantId}
            ORDER BY id
            """;
    }

    /**
     * 生成系统角色新增 SQL。
     *
     * @return 系统角色新增 SQL
     */
    public String insert() {
        return """
            INSERT INTO sys_role (tenant_id, role_code, name, data_scope, status)
            VALUES (#{tenantId}, #{roleCode}, #{name}, #{dataScope}, #{status})
            """;
    }

    /**
     * 生成系统角色更新 SQL。
     *
     * @return 系统角色更新 SQL
     */
    public String update() {
        return """
            UPDATE sys_role
            SET role_code = #{roleCode},
                name = #{name},
                data_scope = #{dataScope},
                status = #{status}
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
            """;
    }

    /**
     * 生成系统角色状态更新 SQL。
     *
     * @return 系统角色状态更新 SQL
     */
    public String updateStatus() {
        return """
            UPDATE sys_role
            SET status = #{status}
            WHERE tenant_id = #{tenantId}
              AND id = #{roleId}
            """;
    }

    /**
     * 生成租户内角色菜单绑定删除 SQL。
     *
     * @return 角色菜单绑定删除 SQL
     */
    public String deleteRoleMenusByTenantAndRole() {
        return """
            DELETE FROM sys_role_menu
            WHERE tenant_id = #{tenantId}
              AND role_id = #{roleId}
            """;
    }

    /**
     * 生成租户内角色数据权限绑定删除 SQL。
     *
     * @return 角色数据权限绑定删除 SQL
     */
    public String deleteRoleDataScopesByTenantAndRole() {
        return """
            DELETE FROM sys_role_data_scope
            WHERE tenant_id = #{tenantId}
              AND role_id = #{roleId}
            """;
    }

    /**
     * 生成租户内用户角色绑定删除 SQL。
     *
     * @return 用户角色绑定删除 SQL
     */
    public String deleteUserRolesByTenantAndRole() {
        return """
            DELETE FROM sys_user_role
            WHERE tenant_id = #{tenantId}
              AND role_id = #{roleId}
            """;
    }

    /**
     * 生成系统角色删除 SQL。
     *
     * @return 系统角色删除 SQL
     */
    public String deleteByTenantIdAndId() {
        return """
            DELETE FROM sys_role
            WHERE tenant_id = #{tenantId}
              AND id = #{roleId}
            """;
    }

    /**
     * 生成租户内角色菜单绑定查询 SQL。
     *
     * @return 角色菜单绑定查询 SQL
     */
    public String selectRoleMenuIdsByTenantAndRole() {
        return """
            SELECT menu_id
            FROM sys_role_menu
            WHERE tenant_id = #{tenantId}
              AND role_id = #{roleId}
            ORDER BY menu_id
            """;
    }

    /**
     * 生成角色菜单绑定新增 SQL。
     *
     * @return 角色菜单绑定新增 SQL
     */
    public String insertRoleMenu() {
        return """
            INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
            VALUES (#{tenantId}, #{roleId}, #{menuId})
            """;
    }
}
