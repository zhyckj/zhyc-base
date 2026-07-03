/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.mapper;

/**
 * 系统用户岗位 SQL Provider。
 */
public class SysUserPostSqlProvider {

    /**
     * 生成租户内指定用户岗位绑定查询 SQL。
     *
     * @return 用户岗位绑定查询 SQL
     */
    public String selectByTenantIdAndUserId() {
        return """
            SELECT up.id,
                   up.tenant_id AS tenantId,
                   up.user_id AS userId,
                   up.post_id AS postId,
                   p.post_code AS postCode,
                   p.post_name AS postName,
                   up.primary_flag AS primaryFlag,
                   up.created_at AS createdAt
            FROM sys_user_post up
            JOIN sys_post p
              ON p.tenant_id = up.tenant_id
             AND p.id = up.post_id
            WHERE up.tenant_id = #{tenantId}
              AND up.user_id = #{userId}
            ORDER BY up.primary_flag DESC, p.sort_order, p.id
            """;
    }

    /**
     * 生成租户内指定用户岗位绑定删除 SQL。
     *
     * @return 用户岗位绑定删除 SQL
     */
    public String deleteByTenantIdAndUserId() {
        return """
            DELETE FROM sys_user_post
            WHERE tenant_id = #{tenantId}
              AND user_id = #{userId}
            """;
    }

    /**
     * 生成租户内用户岗位绑定新增 SQL。
     *
     * @return 用户岗位绑定新增 SQL
     */
    public String insertUserPost() {
        return """
            INSERT INTO sys_user_post (tenant_id, user_id, post_id, primary_flag)
            VALUES (#{tenantId}, #{userId}, #{postId}, #{primaryFlag})
            """;
    }
}
