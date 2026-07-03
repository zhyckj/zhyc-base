/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.mapper;

/**
 * 系统用户角色 SQL Provider。
 */
public class SysUserRoleSqlProvider {

    /**
     * 生成租户内指定用户角色绑定查询 SQL。
     *
     * @return 用户角色绑定查询 SQL
     */
    public String selectByTenantIdAndUserId() {
        return """
            SELECT ur.id,
                   ur.tenant_id AS tenantId,
                   ur.user_id AS userId,
                   ur.role_id AS roleId,
                   r.role_code AS roleCode,
                   r.name AS roleName,
                   r.data_scope AS dataScope,
                   r.status,
                   ur.created_at AS createdAt
            FROM sys_user_role ur
            JOIN sys_role r
              ON r.tenant_id = ur.tenant_id
             AND r.id = ur.role_id
            WHERE ur.tenant_id = #{tenantId}
              AND ur.user_id = #{userId}
            ORDER BY r.id
            """;
    }

    /**
     * 生成租户内指定用户角色绑定删除 SQL。
     *
     * @return 用户角色绑定删除 SQL
     */
    public String deleteByTenantIdAndUserId() {
        return """
            DELETE FROM sys_user_role
            WHERE tenant_id = #{tenantId}
              AND user_id = #{userId}
            """;
    }

    /**
     * 生成租户内用户角色绑定新增 SQL。
     *
     * @return 用户角色绑定新增 SQL
     */
    public String insertUserRole() {
        return """
            INSERT INTO sys_user_role (tenant_id, user_id, role_id)
            VALUES (#{tenantId}, #{userId}, #{roleId})
            """;
    }
}
