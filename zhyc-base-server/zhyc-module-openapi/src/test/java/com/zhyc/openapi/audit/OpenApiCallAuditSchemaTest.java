/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 调用审计表结构测试。
 */
class OpenApiCallAuditSchemaTest {

    /**
     * 验证开放 API 调用审计表字段与审计设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineOpenApiCallAuditTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__openapi_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists openapi_call_audit"),
                "should create openapi_call_audit table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "openapi_call_audit should include tenant_id");
        assertTrue(sql.contains("app_code varchar(64) not null"),
                "openapi_call_audit should include app_code");
        assertTrue(sql.contains("access_key varchar(128) not null"),
                "openapi_call_audit should include access_key");
        assertTrue(sql.contains("response_status int not null"),
                "openapi_call_audit should include response_status");
        assertTrue(sql.contains("duration_ms bigint not null"),
                "openapi_call_audit should include duration_ms");
        assertTrue(sql.contains("key idx_openapi_call_audit_app_called (tenant_id, app_code, called_at)"),
                "openapi_call_audit should include app called index");
    }
}
