/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 版本表结构测试。
 */
class OpenApiVersionSchemaTest {

    /**
     * 验证 API 版本表字段与发布设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineOpenApiVersionTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__openapi_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists openapi_version"),
                "should create openapi_version table");
        assertTrue(sql.contains("api_code varchar(128) not null"),
                "openapi_version should include api_code");
        assertTrue(sql.contains("version varchar(32) not null"),
                "openapi_version should include version");
        assertTrue(sql.contains("backend_route varchar(512) not null"),
                "openapi_version should include backend_route");
        assertTrue(sql.contains("request_schema json"),
                "openapi_version should include request_schema");
        assertTrue(sql.contains("response_schema json"),
                "openapi_version should include response_schema");
        assertTrue(sql.contains("unique key uk_openapi_version_api_version (api_code, version)"),
                "openapi_version should include api version unique key");
    }
}
