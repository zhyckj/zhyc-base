/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog;

import com.zhyc.system.loginlog.mapper.SysLoginLogSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统登录日志 SQL Provider 测试。
 */
class SysLoginLogSqlProviderTest {

    /**
     * 验证最近登录日志查询 SQL 使用显式列并包含租户隔离条件。
     */
    @Test
    void shouldGenerateRecentSqlWithTenantIsolationAndExplicitColumns() {
        SysLoginLogSqlProvider provider = new SysLoginLogSqlProvider();

        String sql = provider.selectRecentByTenantId();

        assertTrue(sql.contains("FROM sys_login_log"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("ORDER BY created_at DESC, id DESC"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证登录日志新增 SQL 包含登录类型、结果、IP 和 UA 字段。
     */
    @Test
    void shouldGenerateInsertSqlWithSecurityAuditFields() {
        SysLoginLogSqlProvider provider = new SysLoginLogSqlProvider();

        String sql = provider.insert();

        assertTrue(sql.contains("login_type"));
        assertTrue(sql.contains("result"));
        assertTrue(sql.contains("client_ip"));
        assertTrue(sql.contains("user_agent"));
    }
}
