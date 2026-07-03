/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit;

import com.zhyc.openapi.audit.mapper.OpenApiCallAuditSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 调用审计 SQL Provider 测试。
 */
class OpenApiCallAuditSqlProviderTest {

    /**
     * 验证开放 API 调用审计列表 SQL 使用租户和应用编码隔离。
     */
    @Test
    void shouldGenerateSelectSqlWithTenantAndAppIsolation() {
        OpenApiCallAuditSqlProvider provider = new OpenApiCallAuditSqlProvider();

        String sql = provider.selectByTenantIdAndAppCode();

        assertTrue(sql.contains("FROM openapi_call_audit"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND app_code = #{appCode}"));
        assertTrue(sql.contains("ORDER BY called_at DESC"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证开放 API 错误日志 SQL 只查询失败调用记录。
     */
    @Test
    void shouldGenerateErrorLogSelectSqlWithFailureFilter() {
        OpenApiCallAuditSqlProvider provider = new OpenApiCallAuditSqlProvider();

        String sql = provider.selectErrorLogsByTenantIdAndAppCode();

        assertTrue(sql.contains("FROM openapi_call_audit"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND app_code = #{appCode}"));
        assertTrue(sql.contains("AND success = 0"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证开放 API 调用审计记录 SQL 使用显式列写入。
     */
    @Test
    void shouldGenerateInsertSql() {
        OpenApiCallAuditSqlProvider provider = new OpenApiCallAuditSqlProvider();

        String sql = provider.insert();

        assertTrue(sql.contains("INSERT INTO openapi_call_audit"));
        assertTrue(sql.contains("response_status"));
        assertTrue(sql.contains("duration_ms"));
        assertFalse(sql.contains("INSERT INTO openapi_call_audit VALUES"));
    }
}
