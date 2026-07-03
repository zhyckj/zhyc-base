/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 权限授权表结构测试。
 */
class OpenApiPermissionSchemaTest {

    /**
     * 验证开放 API 权限授权表字段与鉴权设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineOpenApiPermissionTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__openapi_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists openapi_api_permission"),
                "should create openapi_api_permission table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "openapi_api_permission should include tenant_id");
        assertTrue(sql.contains("app_code varchar(64) not null"),
                "openapi_api_permission should include app_code");
        assertTrue(sql.contains("api_code varchar(128) not null"),
                "openapi_api_permission should include api_code");
        assertTrue(sql.contains("http_method varchar(16) not null"),
                "openapi_api_permission should include http_method");
        assertTrue(sql.contains("path_pattern varchar(256) not null"),
                "openapi_api_permission should include path_pattern");
        assertTrue(sql.contains("unique key uk_openapi_api_permission_app_api (tenant_id, app_code, api_code)"),
                "openapi_api_permission should include tenant app api unique key");
    }
}
