/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction;

import com.zhyc.system.accessrestriction.mapper.SysAccessRestrictionSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统访问限制 SQL Provider 测试。
 */
class SysAccessRestrictionSqlProviderTest {

    /**
     * 验证访问限制查询 SQL 使用显式列，并包含租户、类型和生效时间条件。
     */
    @Test
    void shouldGenerateActiveSelectSqlWithTenantTypeAndTime() {
        SysAccessRestrictionSqlProvider provider = new SysAccessRestrictionSqlProvider();

        String sql = provider.selectActiveRestrictions();

        assertTrue(sql.contains("FROM sys_access_restriction"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND restriction_type = #{restrictionType}"));
        assertTrue(sql.contains("(start_at IS NULL OR start_at <= #{now})"));
        assertTrue(sql.contains("(end_at IS NULL OR end_at >= #{now})"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证访问限制保存 SQL 通过唯一键做新增或更新。
     */
    @Test
    void shouldGenerateSaveSqlWithUpsert() {
        SysAccessRestrictionSqlProvider provider = new SysAccessRestrictionSqlProvider();

        String sql = provider.saveRestriction();

        assertTrue(sql.contains("INSERT INTO sys_access_restriction"));
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("restriction_type"));
        assertTrue(sql.contains("rule_value"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
