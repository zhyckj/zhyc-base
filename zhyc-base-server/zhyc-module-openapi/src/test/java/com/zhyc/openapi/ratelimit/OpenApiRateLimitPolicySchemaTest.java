/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放平台 API 限流策略表结构测试。
 */
class OpenApiRateLimitPolicySchemaTest {

    /**
     * 验证开放 API 限流策略表字段与网关运行态限流设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineOpenApiRateLimitPolicyTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__openapi_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists openapi_rate_limit_policy"),
                "should create openapi_rate_limit_policy table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "openapi_rate_limit_policy should include tenant_id");
        assertTrue(sql.contains("app_code varchar(64) not null"),
                "openapi_rate_limit_policy should include app_code");
        assertTrue(sql.contains("api_code varchar(128) not null"),
                "openapi_rate_limit_policy should include api_code");
        assertTrue(sql.contains("limit_count int not null"),
                "openapi_rate_limit_policy should include limit_count");
        assertTrue(sql.contains("window_seconds int not null"),
                "openapi_rate_limit_policy should include window_seconds");
        assertTrue(sql.contains("unique key uk_openapi_rate_limit_policy_app_api (tenant_id, app_code, api_code)"),
                "openapi_rate_limit_policy should include tenant app api unique key");
    }
}
