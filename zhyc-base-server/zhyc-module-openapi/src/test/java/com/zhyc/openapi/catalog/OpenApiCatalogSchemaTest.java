/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 目录表结构测试。
 */
class OpenApiCatalogSchemaTest {

    /**
     * 验证 API 目录表字段与开放平台发布管理设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineOpenApiCatalogTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__openapi_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists openapi_catalog"),
                "should create openapi_catalog table");
        assertTrue(sql.contains("api_code varchar(128) not null"),
                "openapi_catalog should include api_code");
        assertTrue(sql.contains("api_name varchar(128) not null"),
                "openapi_catalog should include api_name");
        assertTrue(sql.contains("group_code varchar(64) not null"),
                "openapi_catalog should include group_code");
        assertTrue(sql.contains("http_method varchar(16) not null"),
                "openapi_catalog should include http_method");
        assertTrue(sql.contains("path_pattern varchar(256) not null"),
                "openapi_catalog should include path_pattern");
        assertTrue(sql.contains("status varchar(32) not null"),
                "openapi_catalog should include status");
        assertTrue(sql.contains("unique key uk_openapi_catalog_api_code (api_code)"),
                "openapi_catalog should include api code unique key");
    }
}
