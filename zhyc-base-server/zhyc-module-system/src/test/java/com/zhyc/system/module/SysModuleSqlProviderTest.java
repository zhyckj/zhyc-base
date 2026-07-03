/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module;

import com.zhyc.system.module.mapper.SysModuleSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统模块 SQL Provider 测试。
 */
class SysModuleSqlProviderTest {

    /**
     * 验证模块 SQL 使用显式列，并按模块编码处理依赖、资源和启停状态。
     */
    @Test
    void shouldGenerateSqlWithExplicitColumnsAndModuleCodeConditions() {
        SysModuleSqlProvider provider = new SysModuleSqlProvider();

        String moduleSql = provider.selectAllModules();
        String dependencySql = provider.selectDependenciesByModuleCode();
        String resourceSql = provider.selectResourcesByModuleCode();
        String updateSql = provider.updateEnabled();
        String updateModuleSql = provider.updateModule();
        String deleteDependencySql = provider.deleteDependenciesByModuleCode();
        String deleteResourceSql = provider.deleteResourcesByModuleCode();

        assertTrue(moduleSql.contains("FROM sys_module"));
        assertTrue(dependencySql.contains("WHERE module_code = #{moduleCode}"));
        assertTrue(resourceSql.contains("WHERE module_code = #{moduleCode}"));
        assertTrue(updateSql.contains("WHERE module_code = #{moduleCode}"));
        assertTrue(updateSql.contains("enabled = #{enabled}"));
        assertTrue(updateModuleSql.contains("WHERE module_code = #{moduleCode}"));
        assertTrue(deleteDependencySql.contains("WHERE module_code = #{moduleCode}"));
        assertTrue(deleteResourceSql.contains("WHERE module_code = #{moduleCode}"));
        assertFalse(moduleSql.contains("SELECT *"));
    }
}
