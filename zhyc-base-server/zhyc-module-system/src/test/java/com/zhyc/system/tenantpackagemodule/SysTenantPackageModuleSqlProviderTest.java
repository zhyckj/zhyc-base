/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule;

import com.zhyc.system.tenantpackagemodule.mapper.SysTenantPackageModuleSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户套餐模块授权 SQL Provider 测试。
 */
class SysTenantPackageModuleSqlProviderTest {

    /**
     * 验证套餐授权查询 SQL 使用显式列并按套餐过滤。
     */
    @Test
    void shouldGenerateSelectByPackageIdSqlWithExplicitColumns() {
        SysTenantPackageModuleSqlProvider provider = new SysTenantPackageModuleSqlProvider();

        String sql = provider.selectByPackageId();

        assertTrue(sql.contains("FROM sys_tenant_package_module"));
        assertTrue(sql.contains("WHERE package_id = #{packageId}"));
        assertTrue(sql.contains("module_code AS moduleCode"));
        assertTrue(sql.contains("menu_code AS menuCode"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证套餐授权删除 SQL 使用套餐主键。
     */
    @Test
    void shouldGenerateDeleteByPackageIdSql() {
        SysTenantPackageModuleSqlProvider provider = new SysTenantPackageModuleSqlProvider();

        String sql = provider.deleteByPackageId();

        assertTrue(sql.contains("DELETE FROM sys_tenant_package_module"));
        assertTrue(sql.contains("WHERE package_id = #{packageId}"));
    }

    /**
     * 验证套餐授权新增 SQL 覆盖模块、菜单和权限字段。
     */
    @Test
    void shouldGenerateInsertSqlWithGrantColumns() {
        SysTenantPackageModuleSqlProvider provider = new SysTenantPackageModuleSqlProvider();

        String sql = provider.insert();

        assertTrue(sql.contains("INSERT INTO sys_tenant_package_module"));
        assertTrue(sql.contains("package_id"));
        assertTrue(sql.contains("module_code"));
        assertTrue(sql.contains("menu_code"));
        assertTrue(sql.contains("permission"));
    }
}
