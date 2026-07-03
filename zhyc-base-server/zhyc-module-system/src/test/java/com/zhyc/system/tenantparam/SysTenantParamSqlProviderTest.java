/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam;

import com.zhyc.system.tenantparam.mapper.SysTenantParamSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户参数 SQL Provider 测试。
 */
class SysTenantParamSqlProviderTest {

    /**
     * 验证租户参数列表查询 SQL 使用显式列并按租户过滤。
     */
    @Test
    void shouldGenerateListSqlWithTenantFilterAndExplicitColumns() {
        SysTenantParamSqlProvider provider = new SysTenantParamSqlProvider();

        String sql = provider.selectByTenantId();

        assertTrue(sql.contains("FROM sys_tenant_param"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("param_key AS paramKey"));
        assertTrue(sql.contains("param_value AS paramValue"));
        assertTrue(sql.contains("value_type AS valueType"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证租户参数保存 SQL 使用租户和参数键唯一约束做覆盖更新。
     */
    @Test
    void shouldGenerateUpsertSqlWithTenantParamKey() {
        SysTenantParamSqlProvider provider = new SysTenantParamSqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO sys_tenant_param"));
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("param_key"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
