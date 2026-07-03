/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog;

import com.zhyc.openapi.catalog.mapper.OpenApiCatalogSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 目录 SQL Provider 测试。
 */
class OpenApiCatalogSqlProviderTest {

    /**
     * 验证 API 目录查询 SQL 支持按分组过滤。
     */
    @Test
    void shouldSelectCatalogsByGroupCode() {
        OpenApiCatalogSqlProvider provider = new OpenApiCatalogSqlProvider();

        String sql = provider.selectByGroupCode();

        assertTrue(sql.contains("FROM openapi_catalog"));
        assertTrue(sql.contains("WHERE group_code = #{groupCode}"));
        assertTrue(sql.contains("ORDER BY group_code, api_code"));
    }

    /**
     * 验证 API 目录保存 SQL 使用唯一键更新。
     */
    @Test
    void shouldUpsertCatalog() {
        OpenApiCatalogSqlProvider provider = new OpenApiCatalogSqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO openapi_catalog"));
        assertTrue(sql.contains("api_code, api_name, group_code, http_method, path_pattern, status"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
