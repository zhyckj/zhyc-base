/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org;

import com.zhyc.system.org.mapper.SysOrgSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统组织机构 SQL Provider 测试。
 */
class SysOrgSqlProviderTest {

    /**
     * 验证组织查询 SQL 使用显式列并包含租户隔离条件。
     */
    @Test
    void shouldGenerateSqlWithTenantIsolationAndExplicitColumns() {
        SysOrgSqlProvider provider = new SysOrgSqlProvider();

        String sql = provider.selectByTenantId();

        assertTrue(sql.contains("FROM sys_org"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("ORDER BY sort_order, id"));
        assertFalse(sql.contains("SELECT *"));
    }
}
