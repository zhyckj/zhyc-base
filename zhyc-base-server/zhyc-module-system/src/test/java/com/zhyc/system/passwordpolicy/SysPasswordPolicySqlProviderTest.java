/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy;

import com.zhyc.system.passwordpolicy.mapper.SysPasswordPolicySqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统密码策略 SQL Provider 测试。
 */
class SysPasswordPolicySqlProviderTest {

    /**
     * 验证密码策略查询 SQL 使用显式列，并包含租户隔离条件。
     */
    @Test
    void shouldGenerateSelectSqlWithTenantIsolation() {
        SysPasswordPolicySqlProvider provider = new SysPasswordPolicySqlProvider();

        String sql = provider.selectDefaultByTenantId();

        assertTrue(sql.contains("FROM sys_password_policy"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND policy_code = 'default'"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证密码策略保存 SQL 通过租户和策略编码唯一键做新增或更新。
     */
    @Test
    void shouldGenerateUpsertSql() {
        SysPasswordPolicySqlProvider provider = new SysPasswordPolicySqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO sys_password_policy"));
        assertTrue(sql.contains("min_length"));
        assertTrue(sql.contains("require_uppercase"));
        assertTrue(sql.contains("max_retry_count"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
