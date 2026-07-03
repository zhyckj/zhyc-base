/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.mapper;

/**
 * 系统角色自定义数据权限 SQL Provider。
 */
public class SysRoleDataScopeSqlProvider {

    /**
     * 生成租户内角色自定义组织范围查询 SQL。
     *
     * @return 角色自定义数据权限查询 SQL
     */
    public String selectByTenantIdAndRoleId() {
        return """
            SELECT rds.id,
                   rds.tenant_id AS tenantId,
                   rds.role_id AS roleId,
                   rds.org_id AS orgId,
                   o.org_name AS orgName,
                   rds.scope_type AS scopeType,
                   rds.created_at AS createdAt
            FROM sys_role_data_scope rds
            JOIN sys_org o
              ON o.tenant_id = rds.tenant_id
             AND o.id = rds.org_id
            WHERE rds.tenant_id = #{tenantId}
              AND rds.role_id = #{roleId}
            ORDER BY o.sort_order, o.id
            """;
    }

    /**
     * 生成租户内角色自定义组织范围删除 SQL。
     *
     * @return 角色自定义数据权限删除 SQL
     */
    public String deleteByTenantIdAndRoleId() {
        return """
            DELETE FROM sys_role_data_scope
            WHERE tenant_id = #{tenantId}
              AND role_id = #{roleId}
            """;
    }

    /**
     * 生成租户内角色自定义组织范围新增 SQL。
     *
     * @return 角色自定义数据权限新增 SQL
     */
    public String insertRoleDataScope() {
        return """
            INSERT INTO sys_role_data_scope (tenant_id, role_id, org_id, scope_type)
            VALUES (#{tenantId}, #{roleId}, #{orgId}, #{scopeType})
            """;
    }
}
