/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post.mapper;

/**
 * 系统岗位 SQL Provider。
 */
public class SysPostSqlProvider {

    /**
     * 生成租户内岗位查询 SQL。
     *
     * @return 岗位查询 SQL
     */
    public String selectByTenantIdAndOrgId() {
        return """
            <script>
            SELECT id,
                   tenant_id AS tenantId,
                   org_id AS orgId,
                   post_code AS postCode,
                   post_name AS postName,
                   sort_order AS sortOrder,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_post
            WHERE tenant_id = #{tenantId}
            <if test="orgId != null">
              AND org_id = #{orgId}
            </if>
            ORDER BY sort_order, id
            </script>
            """;
    }

    public String insert() {
        return """
            INSERT INTO sys_post (tenant_id, org_id, post_code, post_name, sort_order, status)
            VALUES (#{tenantId}, #{orgId}, #{postCode}, #{postName}, #{sortOrder}, #{status})
            """;
    }

    public String update() {
        return """
            UPDATE sys_post
            SET org_id = #{orgId},
                post_code = #{postCode},
                post_name = #{postName},
                sort_order = #{sortOrder},
                status = #{status}
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
            """;
    }

    public String updateStatus() {
        return """
            UPDATE sys_post
            SET status = #{status}
            WHERE tenant_id = #{tenantId}
              AND id = #{postId}
            """;
    }

    public String deleteUserPostsByTenantAndPost() {
        return """
            DELETE FROM sys_user_post
            WHERE tenant_id = #{tenantId}
              AND post_id = #{postId}
            """;
    }

    public String deleteByTenantIdAndId() {
        return """
            DELETE FROM sys_post
            WHERE tenant_id = #{tenantId}
              AND id = #{postId}
            """;
    }
}
