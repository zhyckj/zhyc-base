/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope;

import com.zhyc.system.adminscope.mapper.SysAdminScopeSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统管理员管理范围 SQL Provider 测试。
 */
class SysAdminScopeSqlProviderTest {

    /**
     * 验证管理员范围查询 SQL 使用显式列，并包含租户和管理员用户隔离条件。
     */
    @Test
    void shouldGenerateSelectSqlWithTenantAndUserIsolation() {
        SysAdminScopeSqlProvider provider = new SysAdminScopeSqlProvider();

        String sql = provider.selectByTenantIdAndUserId();

        assertTrue(sql.contains("FROM sys_admin_scope"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND user_id = #{userId}"));
        assertTrue(sql.contains("ORDER BY scope_type, scope_ref_code"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证管理员范围写入 SQL 包含租户、管理员用户、范围类型和范围引用。
     */
    @Test
    void shouldGenerateInsertSqlWithTenantUserAndScope() {
        SysAdminScopeSqlProvider provider = new SysAdminScopeSqlProvider();

        String sql = provider.insertAdminScope();

        assertTrue(sql.contains("INSERT INTO sys_admin_scope"));
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("user_id"));
        assertTrue(sql.contains("scope_type"));
        assertTrue(sql.contains("scope_ref_code"));
    }
}
