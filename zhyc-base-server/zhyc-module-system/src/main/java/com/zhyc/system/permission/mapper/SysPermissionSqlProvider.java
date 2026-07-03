/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permission.mapper;

/**
 * 系统权限 SQL Provider。
 */
public class SysPermissionSqlProvider {

    /**
     * 生成租户内用户权限查询 SQL。
     *
     * @return 权限查询 SQL
     */
    public String selectPermissionsByTenantAndUser() {
        return """
            SELECT DISTINCT granted.permission
            FROM (
              SELECT m.permission
              FROM sys_user_role ur
              JOIN sys_role_menu rm
                ON rm.tenant_id = ur.tenant_id
               AND rm.role_id = ur.role_id
              JOIN sys_menu m
                ON m.tenant_id = rm.tenant_id
               AND m.id = rm.menu_id
              WHERE ur.tenant_id = #{tenantId}
                AND ur.user_id = #{userId}
                AND m.permission IS NOT NULL
                AND m.permission <> ''
                AND m.status = 'enabled'
              UNION ALL
              SELECT '*'
              FROM sys_user_role ur
              JOIN sys_role r
                ON r.tenant_id = ur.tenant_id
               AND r.id = ur.role_id
              WHERE ur.tenant_id = #{tenantId}
                AND ur.user_id = #{userId}
                AND r.status = 'enabled'
                AND r.role_code IN ('platform-admin', 'super_admin')
            ) granted
            ORDER BY granted.permission
            """;
    }
}
