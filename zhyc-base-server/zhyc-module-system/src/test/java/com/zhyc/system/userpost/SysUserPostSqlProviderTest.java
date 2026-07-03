/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.userpost;

import com.zhyc.system.user.mapper.SysUserPostSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统用户岗位 SQL Provider 测试。
 */
class SysUserPostSqlProviderTest {

    /**
     * 验证用户岗位查询 SQL 使用显式列，并包含租户和用户隔离条件。
     */
    @Test
    void shouldGenerateSelectSqlWithTenantAndUserIsolation() {
        SysUserPostSqlProvider provider = new SysUserPostSqlProvider();

        String sql = provider.selectByTenantIdAndUserId();

        assertTrue(sql.contains("FROM sys_user_post up"));
        assertTrue(sql.contains("JOIN sys_post p"));
        assertTrue(sql.contains("WHERE up.tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND up.user_id = #{userId}"));
        assertTrue(sql.contains("ORDER BY up.primary_flag DESC, p.sort_order, p.id"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证用户岗位写入 SQL 包含租户字段和主岗位标记。
     */
    @Test
    void shouldGenerateInsertSqlWithTenantAndPrimaryFlag() {
        SysUserPostSqlProvider provider = new SysUserPostSqlProvider();

        String sql = provider.insertUserPost();

        assertTrue(sql.contains("INSERT INTO sys_user_post"));
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("primary_flag"));
        assertTrue(sql.contains("#{primaryFlag}"));
    }
}
