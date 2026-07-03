/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户套餐模块授权表结构测试。
 */
class SysTenantPackageModuleSchemaTest {

    /**
     * 验证租户套餐模块授权表字段与首期 SaaS 套餐授权设计保持一致。
     *
     * @throws IOException 读取建表脚本失败时抛出
     */
    @Test
    void shouldDeclareTenantPackageModuleTableInSchema() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__system_core.sql"), StandardCharsets.UTF_8)
                .toLowerCase();

        assertTrue(sql.contains("create table if not exists sys_tenant_package_module"),
                "should create sys_tenant_package_module table");
        assertTrue(sql.contains("package_id bigint not null comment '租户套餐主键'"),
                "sys_tenant_package_module should include package_id");
        assertTrue(sql.contains("module_code varchar(64) not null comment '模块编码'"),
                "sys_tenant_package_module should include module_code");
        assertTrue(sql.contains("menu_code varchar(64) default null comment '菜单编码'"),
                "sys_tenant_package_module should include menu_code");
        assertTrue(sql.contains("permission varchar(128) default null comment '权限标识'"),
                "sys_tenant_package_module should include permission");
        assertTrue(sql.contains("unique key uk_sys_tenant_package_module_resource"),
                "sys_tenant_package_module should include unique resource index");
    }
}
