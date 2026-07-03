/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开发者应用表结构测试。
 */
class OpenApiAppSchemaTest {

    /**
     * 验证开发者应用表字段与开放平台设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineOpenApiAppTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__openapi_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists openapi_app"),
                "should create openapi_app table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "openapi_app should include tenant_id");
        assertTrue(sql.contains("app_code varchar(64) not null"),
                "openapi_app should include app_code");
        assertTrue(sql.contains("owner_user_id bigint not null"),
                "openapi_app should include owner_user_id");
        assertTrue(sql.contains("auth_mode varchar(32) not null"),
                "openapi_app should include auth_mode");
        assertTrue(sql.contains("unique key uk_openapi_app_tenant_code (tenant_id, app_code)"),
                "openapi_app should include unique tenant app code key");
    }
}
