/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.mapper;

/**
 * 系统管理员管理范围 SQL Provider。
 */
public class SysAdminScopeSqlProvider {

    /**
     * 生成租户内管理员管理范围查询 SQL。
     *
     * @return 管理员管理范围查询 SQL
     */
    public String selectByTenantIdAndUserId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   user_id AS userId,
                   scope_type AS scopeType,
                   scope_ref_code AS scopeRefCode,
                   scope_name AS scopeName,
                   created_at AS createdAt
            FROM sys_admin_scope
            WHERE tenant_id = #{tenantId}
              AND user_id = #{userId}
            ORDER BY scope_type, scope_ref_code
            """;
    }

    /**
     * 生成租户内管理员管理范围删除 SQL。
     *
     * @return 管理员管理范围删除 SQL
     */
    public String deleteByTenantIdAndUserId() {
        return """
            DELETE FROM sys_admin_scope
            WHERE tenant_id = #{tenantId}
              AND user_id = #{userId}
            """;
    }

    /**
     * 生成租户内管理员管理范围新增 SQL。
     *
     * @return 管理员管理范围新增 SQL
     */
    public String insertAdminScope() {
        return """
            INSERT INTO sys_admin_scope (tenant_id, user_id, scope_type, scope_ref_code)
            VALUES (#{tenantId}, #{userId}, #{scopeType}, #{scopeRefCode})
            """;
    }
}
