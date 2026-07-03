/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permission;

import com.zhyc.system.permission.mapper.SysPermissionSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统权限 SQL Provider 测试。
 */
class SysPermissionSqlProviderTest {

    /**
     * 验证用户权限 SQL 通过用户角色、角色菜单和菜单表查询，并在每段关联中带租户隔离。
     */
    @Test
    void shouldJoinRoleMenuWithTenantIsolation() {
        String sql = new SysPermissionSqlProvider().selectPermissionsByTenantAndUser()
                .toLowerCase()
                .replaceAll("\\s+", " ");

        assertTrue(sql.contains("select distinct granted.permission"), "query should remove duplicated permissions");
        assertTrue(sql.contains("from ( select m.permission"), "query should wrap unioned permissions");
        assertTrue(sql.contains("from sys_user_role ur"), "should query from user role bindings");
        assertTrue(sql.contains("join sys_role_menu rm on rm.tenant_id = ur.tenant_id and rm.role_id = ur.role_id"),
                "role menu join should be constrained by tenant");
        assertTrue(sql.contains("join sys_menu m on m.tenant_id = rm.tenant_id and m.id = rm.menu_id"),
                "menu join should be constrained by tenant");
        assertTrue(sql.contains("where ur.tenant_id = #{tenantid}"), "query should filter tenant_id");
        assertTrue(sql.contains("and ur.user_id = #{userid}"), "query should filter user_id");
        assertTrue(sql.contains("and m.permission is not null"), "query should skip null permissions");
        assertTrue(sql.contains("and m.status = 'enabled'"), "query should only include enabled menus");
    }

    /**
     * 验证内置平台管理员角色会获得 Shiro 通配权限，避免首期菜单权限码不完整导致管理员无法访问。
     */
    @Test
    void shouldGrantWildcardPermissionToBuiltinAdminRoles() {
        String sql = new SysPermissionSqlProvider().selectPermissionsByTenantAndUser()
                .toLowerCase()
                .replaceAll("\\s+", " ");

        assertTrue(sql.contains("union all select '*'"), "builtin admin role should receive wildcard permission");
        assertTrue(sql.contains("join sys_role r on r.tenant_id = ur.tenant_id and r.id = ur.role_id"),
                "role join should be constrained by tenant");
        assertTrue(sql.contains("and r.status = 'enabled'"), "admin role should be enabled");
        assertTrue(sql.contains("and r.role_code in ('platform-admin', 'super_admin')"),
                "only builtin admin role codes should receive wildcard permission");
        assertTrue(sql.contains("order by granted.permission"), "query should return stable permission order");
    }
}
