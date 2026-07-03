/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放平台 OAuth2 客户端映射表结构测试。
 */
class OpenApiOauthClientSchemaTest {

    /**
     * 验证 OAuth2 客户端映射表字段与开放平台授权设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineOpenApiOauthClientTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__openapi_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists openapi_oauth_client"),
                "should create openapi_oauth_client table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "openapi_oauth_client should include tenant_id");
        assertTrue(sql.contains("app_code varchar(64) not null"),
                "openapi_oauth_client should include app_code");
        assertTrue(sql.contains("client_id varchar(128) not null"),
                "openapi_oauth_client should include client_id");
        assertTrue(sql.contains("allowed_scopes varchar(512) not null"),
                "openapi_oauth_client should include allowed_scopes");
        assertTrue(sql.contains("unique key uk_openapi_oauth_client_app_client (tenant_id, app_code, client_id)"),
                "openapi_oauth_client should include tenant app client unique key");
    }
}
