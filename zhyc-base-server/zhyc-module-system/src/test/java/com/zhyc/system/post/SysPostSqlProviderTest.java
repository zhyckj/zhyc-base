/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post;

import com.zhyc.system.post.mapper.SysPostSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统岗位 SQL Provider 测试。
 */
class SysPostSqlProviderTest {

    /**
     * 验证岗位查询 SQL 使用显式列，并包含租户隔离和可选组织过滤条件。
     */
    @Test
    void shouldGenerateSqlWithTenantIsolationAndOptionalOrgFilter() {
        SysPostSqlProvider provider = new SysPostSqlProvider();

        String sql = provider.selectByTenantIdAndOrgId();

        assertTrue(sql.contains("FROM sys_post"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND org_id = #{orgId}"));
        assertTrue(sql.contains("ORDER BY sort_order, id"));
        assertFalse(sql.contains("SELECT *"));
    }
}
