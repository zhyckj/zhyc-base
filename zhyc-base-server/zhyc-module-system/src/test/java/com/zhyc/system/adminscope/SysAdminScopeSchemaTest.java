/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统管理员管理范围表结构测试。
 */
class SysAdminScopeSchemaTest {

    /**
     * 验证管理员范围表字段与首期 SaaS 租户管理员设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineAdminScopeTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__system_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists sys_admin_scope"),
                "should create sys_admin_scope table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "sys_admin_scope should include tenant_id");
        assertTrue(sql.contains("user_id bigint not null"),
                "sys_admin_scope should include user_id");
        assertTrue(sql.contains("scope_type varchar(32) not null"),
                "sys_admin_scope should include scope_type");
        assertTrue(sql.contains("scope_ref_code varchar(128) not null"),
                "sys_admin_scope should include scope_ref_code");
        assertTrue(sql.contains("unique key uk_sys_admin_scope (tenant_id, user_id, scope_type, scope_ref_code)"),
                "sys_admin_scope should include unique tenant admin scope key");
    }
}
