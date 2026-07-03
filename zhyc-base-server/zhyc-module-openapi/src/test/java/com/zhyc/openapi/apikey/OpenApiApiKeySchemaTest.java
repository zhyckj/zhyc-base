/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * API Key 表结构测试。
 */
class OpenApiApiKeySchemaTest {

    /**
     * 验证 API Key 表字段与开放平台鉴权设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineOpenApiApiKeyTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__openapi_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists openapi_api_key"),
                "should create openapi_api_key table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "openapi_api_key should include tenant_id");
        assertTrue(sql.contains("app_code varchar(64) not null"),
                "openapi_api_key should include app_code");
        assertTrue(sql.contains("access_key varchar(128) not null"),
                "openapi_api_key should include access_key");
        assertTrue(sql.contains("secret_cipher varchar(512) not null"),
                "openapi_api_key should include secret_cipher");
        assertTrue(sql.contains("expire_at datetime"),
                "openapi_api_key should include expire_at");
        assertTrue(sql.contains("unique key uk_openapi_api_key_access_key (access_key)"),
                "openapi_api_key should include access key unique index");
    }
}
