/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant;

import com.zhyc.system.tenant.mapper.SysTenantSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统租户 SQL Provider 测试。
 */
class SysTenantSqlProviderTest {

    /**
     * 验证租户清单查询 SQL 使用显式列，并包含状态过滤条件。
     */
    @Test
    void shouldGenerateListSqlWithStatusFilterAndExplicitColumns() {
        SysTenantSqlProvider provider = new SysTenantSqlProvider();

        String sql = provider.selectByStatus();

        assertTrue(sql.contains("FROM sys_tenant"));
        assertTrue(sql.contains("WHERE status = #{status}"));
        assertTrue(sql.contains("tenant_name AS name"));
        assertTrue(sql.contains("package_id AS packageId"));
        assertTrue(sql.contains("contact_name AS contactName"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证租户状态更新 SQL 使用租户业务编码作为条件。
     */
    @Test
    void shouldGenerateUpdateStatusSqlByTenantId() {
        SysTenantSqlProvider provider = new SysTenantSqlProvider();

        String sql = provider.updateStatus();

        assertTrue(sql.contains("UPDATE sys_tenant"));
        assertTrue(sql.contains("SET status = #{status}"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    }

    /**
     * 验证租户保存 SQL 覆盖首期 SaaS 必需字段，并按租户业务编码幂等更新。
     */
    @Test
    void shouldGenerateUpsertTenantSqlByTenantId() {
        SysTenantSqlProvider provider = new SysTenantSqlProvider();

        String sql = provider.upsertTenant();

        assertTrue(sql.contains("INSERT INTO sys_tenant"));
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("tenant_name"));
        assertTrue(sql.contains("isolation_mode"));
        assertTrue(sql.contains("contact_name"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
        assertTrue(sql.contains("tenant_name = VALUES(tenant_name)"));
        assertTrue(sql.contains("updated_at = CURRENT_TIMESTAMP"));
    }
}
