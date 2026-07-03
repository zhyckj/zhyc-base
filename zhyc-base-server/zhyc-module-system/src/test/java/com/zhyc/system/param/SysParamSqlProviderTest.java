/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.param;

import com.zhyc.system.param.mapper.SysParamSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统参数 SQL Provider 测试。
 */
class SysParamSqlProviderTest {

    /**
     * 验证系统参数 SQL 使用显式列、租户隔离和租户内参数键唯一更新。
     */
    @Test
    void shouldGenerateSqlWithTenantIsolationAndExplicitColumns() {
        SysParamSqlProvider provider = new SysParamSqlProvider();

        String listSql = provider.selectByTenantId();
        String findSql = provider.selectByTenantIdAndParamKey();
        String upsertSql = provider.upsert();

        assertTrue(listSql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(findSql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(findSql.contains("AND param_key = #{paramKey}"));
        assertTrue(upsertSql.contains("tenant_id"));
        assertTrue(upsertSql.contains("ON DUPLICATE KEY UPDATE"));
        assertFalse(listSql.contains("SELECT *"));
    }
}
