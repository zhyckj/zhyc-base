/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AI 能力中心数据库脚本测试。
 */
class AiCoreSchemaTest {

    @Test
    void shouldContainCoreAiTablesAndTenantIndexes() throws IOException {
        String sql = loadSql();

        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS ai_provider"));
        assertTrue(sql.contains("secret_ref VARCHAR(255) NOT NULL COMMENT '密钥中心引用'"));
        assertTrue(sql.contains("UNIQUE KEY uk_ai_provider_tenant_code (tenant_id, provider_code)"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS ai_model_config"));
        assertTrue(sql.contains("KEY idx_ai_model_tenant_provider (tenant_id, provider_id)"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS ai_app"));
        assertTrue(sql.contains("daily_token_quota INT NOT NULL DEFAULT 100000 COMMENT '每日令牌额度'"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS ai_prompt_template"));
        assertTrue(sql.contains("UNIQUE KEY uk_ai_prompt_tenant_code_version (tenant_id, prompt_code, version)"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS ai_invocation_audit"));
        assertTrue(sql.contains("trace_id VARCHAR(128) DEFAULT NULL COMMENT '链路追踪编号'"));
        assertTrue(sql.contains("KEY idx_ai_audit_tenant_app_time (tenant_id, app_code, created_at)"));
    }

    private String loadSql() throws IOException {
        var resource = getClass().getClassLoader().getResource("db/V1__ai_core.sql");
        assertNotNull(resource, "AI 能力中心数据库脚本不能为空");
        return new String(resource.openStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
