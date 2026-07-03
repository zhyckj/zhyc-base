/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.mapper;

/**
 * 系统用户 SQL Provider。
 */
public class SysUserSqlProvider {

    /**
     * 生成租户内系统用户列表查询 SQL。
     *
     * @return 系统用户列表查询 SQL
     */
    public String selectByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   username,
                   nickname,
                   password_hash AS passwordHash,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_user
            WHERE tenant_id = #{tenantId}
            ORDER BY id
            """;
    }

    /**
     * 生成按租户和登录账号查询系统用户的 SQL。
     *
     * @return 系统用户查询 SQL
     */
    public String selectByTenantIdAndUsername() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   username,
                   nickname,
                   password_hash AS passwordHash,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_user
            WHERE tenant_id = #{tenantId}
              AND username = #{username}
            """;
    }

    /**
     * 生成系统用户新增 SQL。
     *
     * @return 系统用户新增 SQL
     */
    public String insert() {
        return """
            INSERT INTO sys_user (tenant_id, username, nickname, password_hash, status)
            VALUES (#{tenantId}, #{username}, #{nickname}, #{passwordHash}, #{status})
            """;
    }

    /**
     * 生成系统用户基础信息更新 SQL。
     *
     * @return 系统用户更新 SQL
     */
    public String update() {
        return """
            UPDATE sys_user
            SET username = #{username},
                nickname = #{nickname},
                status = #{status},
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId}
              AND id = #{userId}
            """;
    }

    /**
     * 生成系统用户状态更新 SQL。
     *
     * @return 系统用户状态更新 SQL
     */
    public String updateStatus() {
        return """
            UPDATE sys_user
            SET status = #{status},
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId}
              AND id = #{userId}
            """;
    }

    /**
     * 生成按租户和登录账号更新用户密码哈希的 SQL。
     *
     * @return 用户密码哈希更新 SQL
     */
    public String updatePasswordHash() {
        return """
            UPDATE sys_user
            SET password_hash = #{passwordHash},
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId}
              AND username = #{username}
            """;
    }

    /**
     * 生成按主键更新用户密码哈希 SQL。
     *
     * @return 用户密码哈希更新 SQL
     */
    public String updatePasswordHashById() {
        return """
            UPDATE sys_user
            SET password_hash = #{passwordHash},
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId}
              AND id = #{userId}
            """;
    }

    /**
     * 生成删除用户关联数据 SQL。
     *
     * @return 用户关联数据删除 SQL
     */
    public String deleteUserPostsByTenantIdAndUserId() {
        return """
            DELETE FROM sys_user_post
            WHERE tenant_id = #{tenantId}
              AND user_id = #{userId}
            """;
    }

    /**
     * 生成删除用户角色关联 SQL。
     *
     * @return 用户角色关联删除 SQL
     */
    public String deleteUserRolesByTenantIdAndUserId() {
        return """
            DELETE FROM sys_user_role
            WHERE tenant_id = #{tenantId}
              AND user_id = #{userId}
            """;
    }

    /**
     * 生成删除管理员范围关联 SQL。
     *
     * @return 管理员范围关联删除 SQL
     */
    public String deleteAdminScopesByTenantIdAndUserId() {
        return """
            DELETE FROM sys_admin_scope
            WHERE tenant_id = #{tenantId}
              AND user_id = #{userId}
            """;
    }

    /**
     * 生成删除用户 SQL。
     *
     * @return 用户删除 SQL
     */
    public String deleteByTenantIdAndId() {
        return """
            DELETE FROM sys_user
            WHERE tenant_id = #{tenantId}
              AND id = #{userId}
            """;
    }
}
