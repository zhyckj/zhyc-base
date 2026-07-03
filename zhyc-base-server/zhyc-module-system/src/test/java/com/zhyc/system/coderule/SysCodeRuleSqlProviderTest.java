/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule;

import com.zhyc.system.coderule.mapper.SysCodeRuleSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统编码规则 SQL Provider 测试。
 */
class SysCodeRuleSqlProviderTest {

    /**
     * 验证编码规则列表查询 SQL 使用显式列，并包含租户隔离条件。
     */
    @Test
    void shouldGenerateSelectSqlWithTenantIsolation() {
        SysCodeRuleSqlProvider provider = new SysCodeRuleSqlProvider();

        String sql = provider.selectByTenantId();

        assertTrue(sql.contains("FROM sys_code_rule"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("ORDER BY rule_code"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证编码规则保存 SQL 通过租户和规则编码唯一键做新增或更新。
     */
    @Test
    void shouldGenerateUpsertSql() {
        SysCodeRuleSqlProvider provider = new SysCodeRuleSqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO sys_code_rule"));
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("rule_code"));
        assertTrue(sql.contains("current_value"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }

    /**
     * 验证编码规则当前值更新 SQL 仅按租户和规则编码更新。
     */
    @Test
    void shouldGenerateCurrentValueUpdateSql() {
        SysCodeRuleSqlProvider provider = new SysCodeRuleSqlProvider();

        String sql = provider.updateCurrentValue();

        assertTrue(sql.contains("UPDATE sys_code_rule"));
        assertTrue(sql.contains("SET current_value = #{currentValue}"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND rule_code = #{ruleCode}"));
    }
}
