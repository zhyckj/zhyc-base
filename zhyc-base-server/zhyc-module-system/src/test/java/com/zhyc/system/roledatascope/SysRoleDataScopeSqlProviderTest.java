/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.roledatascope;

import com.zhyc.system.role.mapper.SysRoleDataScopeSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统角色自定义数据权限 SQL Provider 测试。
 */
class SysRoleDataScopeSqlProviderTest {

    /**
     * 验证角色数据权限查询 SQL 使用显式列，并包含租户和角色隔离条件。
     */
    @Test
    void shouldGenerateSelectSqlWithTenantAndRoleIsolation() {
        SysRoleDataScopeSqlProvider provider = new SysRoleDataScopeSqlProvider();

        String sql = provider.selectByTenantIdAndRoleId();

        assertTrue(sql.contains("FROM sys_role_data_scope rds"));
        assertTrue(sql.contains("JOIN sys_org o"));
        assertTrue(sql.contains("WHERE rds.tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND rds.role_id = #{roleId}"));
        assertTrue(sql.contains("ORDER BY o.sort_order, o.id"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证角色数据权限写入 SQL 包含租户字段、组织主键和范围类型。
     */
    @Test
    void shouldGenerateInsertSqlWithTenantOrgAndScopeType() {
        SysRoleDataScopeSqlProvider provider = new SysRoleDataScopeSqlProvider();

        String sql = provider.insertRoleDataScope();

        assertTrue(sql.contains("INSERT INTO sys_role_data_scope"));
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("org_id"));
        assertTrue(sql.contains("scope_type"));
    }
}
