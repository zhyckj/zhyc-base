/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统编码规则表结构测试。
 */
class SysCodeRuleSchemaTest {

    /**
     * 验证编码规则表字段与基础配置设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineCodeRuleTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__system_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists sys_code_rule"),
                "should create sys_code_rule table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "sys_code_rule should include tenant_id");
        assertTrue(sql.contains("rule_code varchar(64) not null"),
                "sys_code_rule should include rule_code");
        assertTrue(sql.contains("rule_name varchar(128) not null"),
                "sys_code_rule should include rule_name");
        assertTrue(sql.contains("prefix varchar(32)"),
                "sys_code_rule should include prefix");
        assertTrue(sql.contains("date_pattern varchar(32)"),
                "sys_code_rule should include date_pattern");
        assertTrue(sql.contains("sequence_length int not null"),
                "sys_code_rule should include sequence_length");
        assertTrue(sql.contains("current_value int not null"),
                "sys_code_rule should include current_value");
        assertTrue(sql.contains("unique key uk_sys_code_rule_tenant_code (tenant_id, rule_code)"),
                "sys_code_rule should include unique tenant rule code key");
    }
}
