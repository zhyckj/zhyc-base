/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage;

import com.zhyc.system.tenantpackage.mapper.SysTenantPackageSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统租户套餐 SQL Provider 测试。
 */
class SysTenantPackageSqlProviderTest {

    /**
     * 验证套餐清单查询 SQL 使用显式列，并包含状态过滤条件。
     */
    @Test
    void shouldGenerateListSqlWithStatusFilterAndExplicitColumns() {
        SysTenantPackageSqlProvider provider = new SysTenantPackageSqlProvider();

        String sql = provider.selectByStatus();

        assertTrue(sql.contains("FROM sys_tenant_package"));
        assertTrue(sql.contains("WHERE status = #{status}"));
        assertTrue(sql.contains("package_code AS packageCode"));
        assertTrue(sql.contains("package_name AS packageName"));
        assertTrue(sql.contains("max_user_count AS maxUserCount"));
        assertTrue(sql.contains("max_storage_mb AS maxStorageMb"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证套餐状态更新 SQL 使用套餐编码作为条件。
     */
    @Test
    void shouldGenerateUpdateStatusSqlByPackageCode() {
        SysTenantPackageSqlProvider provider = new SysTenantPackageSqlProvider();

        String sql = provider.updateStatus();

        assertTrue(sql.contains("UPDATE sys_tenant_package"));
        assertTrue(sql.contains("SET status = #{status}"));
        assertTrue(sql.contains("WHERE package_code = #{packageCode}"));
    }

    /**
     * 验证套餐创建 SQL 显式写入套餐字段并使用自增主键回填。
     */
    @Test
    void shouldGenerateInsertSqlWithExplicitColumns() {
        SysTenantPackageSqlProvider provider = new SysTenantPackageSqlProvider();

        String sql = provider.insertPackage();

        assertTrue(sql.contains("INSERT INTO sys_tenant_package"));
        assertTrue(sql.contains("package_code"));
        assertTrue(sql.contains("package_name"));
        assertTrue(sql.contains("max_user_count"));
        assertTrue(sql.contains("max_storage_mb"));
        assertTrue(sql.contains("#{packageCode}"));
        assertFalse(sql.contains("SELECT *"));
    }
}
