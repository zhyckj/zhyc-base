/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统租户套餐表结构测试。
 */
class SysTenantPackageSchemaTest {

    /**
     * 验证租户套餐主表字段与首期 SaaS 套餐设计保持一致。
     *
     * @throws IOException 读取建表脚本失败时抛出
     */
    @Test
    void shouldDeclareTenantPackageTableInSchema() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__system_core.sql"), StandardCharsets.UTF_8)
                .toLowerCase();

        assertTrue(sql.contains("create table if not exists sys_tenant_package"),
                "should create sys_tenant_package table");
        assertTrue(sql.contains("package_code varchar(64) not null comment '套餐编码'"),
                "sys_tenant_package should include package_code");
        assertTrue(sql.contains("package_name varchar(128) not null comment '套餐名称'"),
                "sys_tenant_package should include package_name");
        assertTrue(sql.contains("max_user_count int not null default 0 comment '最大用户数'"),
                "sys_tenant_package should include max_user_count");
        assertTrue(sql.contains("max_storage_mb int not null default 0 comment '最大存储容量 mb'"),
                "sys_tenant_package should include max_storage_mb");
        assertTrue(sql.contains("unique key uk_sys_tenant_package_code (package_code)"),
                "sys_tenant_package should include unique package code index");
    }
}
