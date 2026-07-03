/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统访问限制表结构测试。
 */
class SysAccessRestrictionSchemaTest {

    /**
     * 验证访问限制表字段与首期安全访问控制设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineAccessRestrictionTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__system_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists sys_access_restriction"),
                "should create sys_access_restriction table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "sys_access_restriction should include tenant_id");
        assertTrue(sql.contains("restriction_type varchar(32) not null"),
                "sys_access_restriction should include restriction_type");
        assertTrue(sql.contains("rule_value varchar(255) not null"),
                "sys_access_restriction should include rule_value");
        assertTrue(sql.contains("effect varchar(32) not null"),
                "sys_access_restriction should include effect");
        assertTrue(sql.contains("start_at datetime"),
                "sys_access_restriction should include start_at");
        assertTrue(sql.contains("end_at datetime"),
                "sys_access_restriction should include end_at");
    }
}
