/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit;

import com.zhyc.system.audit.mapper.SysAuditLogSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统审计日志 SQL Provider 测试。
 */
class SysAuditLogSqlProviderTest {

    /**
     * 验证审计日志 SQL 显式列字段并包含租户隔离条件。
     */
    @Test
    void shouldGenerateSqlWithTenantIsolationAndExplicitColumns() {
        SysAuditLogSqlProvider provider = new SysAuditLogSqlProvider();

        String insertSql = provider.insert();
        String querySql = provider.selectRecentByTenantId();

        assertTrue(insertSql.contains("tenant_id"));
        assertTrue(querySql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(querySql.contains("ORDER BY created_at DESC, id DESC"));
        assertTrue(querySql.contains("LIMIT #{limit}"));
        assertFalse(querySql.contains("SELECT *"));
    }
}
