/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放平台 API 签名策略表结构测试。
 */
class OpenApiSignaturePolicySchemaTest {

    /**
     * 验证开放 API 签名策略表字段与 API Key 签名运行态设计保持一致。
     *
     * @throws IOException 读取 SQL 文件失败时抛出
     */
    @Test
    void shouldDefineOpenApiSignaturePolicyTable() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__openapi_core.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table if not exists openapi_signature_policy"),
                "should create openapi_signature_policy table");
        assertTrue(sql.contains("tenant_id varchar(64) not null"),
                "openapi_signature_policy should include tenant_id");
        assertTrue(sql.contains("app_code varchar(64) not null"),
                "openapi_signature_policy should include app_code");
        assertTrue(sql.contains("algorithm varchar(32) not null"),
                "openapi_signature_policy should include algorithm");
        assertTrue(sql.contains("timestamp_tolerance_seconds int not null"),
                "openapi_signature_policy should include timestamp_tolerance_seconds");
        assertTrue(sql.contains("nonce_ttl_seconds int not null"),
                "openapi_signature_policy should include nonce_ttl_seconds");
        assertTrue(sql.contains("require_body_hash tinyint not null"),
                "openapi_signature_policy should include require_body_hash");
        assertTrue(sql.contains("unique key uk_openapi_signature_policy_app (tenant_id, app_code)"),
                "openapi_signature_policy should include tenant app unique key");
    }
}
