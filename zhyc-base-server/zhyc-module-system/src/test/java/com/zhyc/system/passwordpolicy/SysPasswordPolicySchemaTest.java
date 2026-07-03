/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统密码策略表结构测试。
 */
class SysPasswordPolicySchemaTest {

    /**
     * 验证密码策略表字段满足账号安全策略配置需求。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefinePasswordPolicyTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__system_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists sys_password_policy"),
                "should create sys_password_policy table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "sys_password_policy should include tenant_id");
        assertTrue(sql.contains("policy_code varchar(64) not null"),
                "sys_password_policy should include policy_code");
        assertTrue(sql.contains("min_length int not null"),
                "sys_password_policy should include min_length");
        assertTrue(sql.contains("require_uppercase tinyint"),
                "sys_password_policy should include require_uppercase");
        assertTrue(sql.contains("expire_days int not null"),
                "sys_password_policy should include expire_days");
        assertTrue(sql.contains("max_retry_count int not null"),
                "sys_password_policy should include max_retry_count");
        assertTrue(sql.contains("unique key uk_sys_password_policy_tenant_code (tenant_id, policy_code)"),
                "sys_password_policy should include unique tenant policy code key");
    }
}
