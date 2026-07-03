/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version;

import com.zhyc.openapi.version.mapper.OpenApiVersionSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 版本 SQL Provider 测试。
 */
class OpenApiVersionSqlProviderTest {

    /**
     * 验证 API 版本查询 SQL 使用 API 编码过滤。
     */
    @Test
    void shouldSelectVersionsByApiCode() {
        OpenApiVersionSqlProvider provider = new OpenApiVersionSqlProvider();

        String sql = provider.selectByApiCode();

        assertTrue(sql.contains("FROM openapi_version"));
        assertTrue(sql.contains("WHERE api_code = #{apiCode}"));
        assertTrue(sql.contains("ORDER BY version"));
    }

    /**
     * 验证 API 版本发布 SQL 使用唯一键更新。
     */
    @Test
    void shouldUpsertVersion() {
        OpenApiVersionSqlProvider provider = new OpenApiVersionSqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO openapi_version"));
        assertTrue(sql.contains("api_code, version, backend_route, request_schema, response_schema, status"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
