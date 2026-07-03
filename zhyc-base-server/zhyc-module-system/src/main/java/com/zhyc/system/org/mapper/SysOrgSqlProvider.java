/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org.mapper;

/**
 * 系统组织机构 SQL Provider。
 */
public class SysOrgSqlProvider {

    /**
     * 生成租户内组织机构查询 SQL。
     *
     * @return 组织机构查询 SQL
     */
    public String selectByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   parent_id AS parentId,
                   ancestors,
                   org_code AS orgCode,
                   org_name AS orgName,
                   leader_user_id AS leaderUserId,
                   sort_order AS sortOrder,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_org
            WHERE tenant_id = #{tenantId}
            ORDER BY sort_order, id
            """;
    }

    public String insert() {
        return """
            INSERT INTO sys_org (
                tenant_id, parent_id, ancestors, org_code, org_name, leader_user_id, sort_order, status
            ) VALUES (
                #{tenantId}, #{parentId}, #{ancestors}, #{orgCode}, #{orgName}, #{leaderUserId}, #{sortOrder}, #{status}
            )
            """;
    }

    public String update() {
        return """
            UPDATE sys_org
            SET parent_id = #{parentId},
                ancestors = #{ancestors},
                org_code = #{orgCode},
                org_name = #{orgName},
                leader_user_id = #{leaderUserId},
                sort_order = #{sortOrder},
                status = #{status}
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
            """;
    }

    public String updateStatus() {
        return """
            UPDATE sys_org
            SET status = #{status}
            WHERE tenant_id = #{tenantId}
              AND id = #{orgId}
            """;
    }

    public String deleteByTenantIdAndId() {
        return """
            DELETE FROM sys_org
            WHERE tenant_id = #{tenantId}
              AND id = #{orgId}
            """;
    }
}
