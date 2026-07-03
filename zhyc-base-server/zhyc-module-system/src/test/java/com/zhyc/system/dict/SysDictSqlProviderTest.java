/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict;

import com.zhyc.system.dict.mapper.SysDictSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统字典 SQL Provider 测试。
 */
class SysDictSqlProviderTest {

    /**
     * 验证系统字典 SQL 使用显式列并包含租户隔离条件。
     */
    @Test
    void shouldGenerateSqlWithTenantIsolationAndExplicitColumns() {
        SysDictSqlProvider provider = new SysDictSqlProvider();

        String typeSql = provider.selectTypesByTenantId();
        String itemSql = provider.selectItemsByTenantIdAndDictCode();

        assertTrue(typeSql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(itemSql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(itemSql.contains("AND dict_code = #{dictCode}"));
        assertTrue(itemSql.contains("ORDER BY sort_order, id"));
        assertFalse(typeSql.contains("SELECT *"));
        assertFalse(itemSql.contains("SELECT *"));
    }
}
