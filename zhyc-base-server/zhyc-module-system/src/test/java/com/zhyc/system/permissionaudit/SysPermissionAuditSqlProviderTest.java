/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit;

import com.zhyc.system.permissionaudit.mapper.SysPermissionAuditSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统权限变更审计 SQL Provider 测试。
 */
class SysPermissionAuditSqlProviderTest {

    /**
     * 验证最近权限变更审计查询 SQL 使用显式列并包含租户隔离条件。
     */
    @Test
    void shouldGenerateRecentSqlWithTenantIsolationAndExplicitColumns() {
        SysPermissionAuditSqlProvider provider = new SysPermissionAuditSqlProvider();

        String sql = provider.selectRecentByTenantId();

        assertTrue(sql.contains("FROM sys_permission_audit"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("ORDER BY created_at DESC, id DESC"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证权限变更审计新增 SQL 包含操作者、目标和变更前后字段。
     */
    @Test
    void shouldGenerateInsertSqlWithPermissionChangeFields() {
        SysPermissionAuditSqlProvider provider = new SysPermissionAuditSqlProvider();

        String sql = provider.insert();

        assertTrue(sql.contains("operator_id"));
        assertTrue(sql.contains("target_type"));
        assertTrue(sql.contains("target_id"));
        assertTrue(sql.contains("before_value"));
        assertTrue(sql.contains("after_value"));
        assertTrue(sql.contains("change_type"));
    }
}
