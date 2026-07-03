/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog;

import com.zhyc.system.exceptionlog.mapper.SysExceptionLogSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统异常日志 SQL Provider 测试。
 */
class SysExceptionLogSqlProviderTest {

    /**
     * 验证最近异常日志查询 SQL 使用显式列并包含租户隔离条件。
     */
    @Test
    void shouldGenerateRecentSqlWithTenantIsolationAndExplicitColumns() {
        SysExceptionLogSqlProvider provider = new SysExceptionLogSqlProvider();

        String sql = provider.selectRecentByTenantId();

        assertTrue(sql.contains("FROM sys_exception_log"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("ORDER BY created_at DESC, id DESC"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证异常日志新增 SQL 包含链路、请求、异常类型、堆栈和客户端字段。
     */
    @Test
    void shouldGenerateInsertSqlWithExceptionDiagnosticsFields() {
        SysExceptionLogSqlProvider provider = new SysExceptionLogSqlProvider();

        String sql = provider.insert();

        assertTrue(sql.contains("trace_id"));
        assertTrue(sql.contains("request_uri"));
        assertTrue(sql.contains("exception_name"));
        assertTrue(sql.contains("stack_trace"));
        assertTrue(sql.contains("client_ip"));
    }
}
