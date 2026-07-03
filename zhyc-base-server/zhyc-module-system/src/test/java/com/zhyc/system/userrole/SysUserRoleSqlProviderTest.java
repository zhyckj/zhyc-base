/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.userrole;

import com.zhyc.system.user.mapper.SysUserRoleSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统用户角色 SQL Provider 测试。
 */
class SysUserRoleSqlProviderTest {

    /**
     * 验证用户角色查询 SQL 使用显式列，并包含租户和用户隔离条件。
     */
    @Test
    void shouldGenerateSelectSqlWithTenantAndUserIsolation() {
        SysUserRoleSqlProvider provider = new SysUserRoleSqlProvider();

        String sql = provider.selectByTenantIdAndUserId();

        assertTrue(sql.contains("FROM sys_user_role ur"));
        assertTrue(sql.contains("JOIN sys_role r"));
        assertTrue(sql.contains("WHERE ur.tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND ur.user_id = #{userId}"));
        assertTrue(sql.contains("ORDER BY r.id"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证用户角色写入 SQL 包含租户、用户和角色字段。
     */
    @Test
    void shouldGenerateInsertSqlWithTenantAndRoleId() {
        SysUserRoleSqlProvider provider = new SysUserRoleSqlProvider();

        String sql = provider.insertUserRole();

        assertTrue(sql.contains("INSERT INTO sys_user_role"));
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("user_id"));
        assertTrue(sql.contains("role_id"));
        assertTrue(sql.contains("#{roleId}"));
    }
}
