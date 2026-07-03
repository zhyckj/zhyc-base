/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role;

import com.zhyc.system.role.mapper.SysRoleSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统角色 SQL Provider 测试。
 */
class SysRoleSqlProviderTest {

    /**
     * 验证角色查询和角色菜单绑定 SQL 都包含租户隔离条件。
     */
    @Test
    void shouldGenerateSqlWithTenantIsolation() {
        SysRoleSqlProvider provider = new SysRoleSqlProvider();

        String listSql = provider.selectByTenantId();
        String deleteSql = provider.deleteRoleMenusByTenantAndRole();
        String insertSql = provider.insertRoleMenu();

        assertTrue(listSql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(deleteSql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(deleteSql.contains("AND role_id = #{roleId}"));
        assertTrue(insertSql.contains("tenant_id"));
        assertTrue(insertSql.contains("#{tenantId}"));
    }
}
