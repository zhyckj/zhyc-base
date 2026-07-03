/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey;

import com.zhyc.openapi.apikey.mapper.OpenApiApiKeySqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * API Key SQL Provider 测试。
 */
class OpenApiApiKeySqlProviderTest {

    /**
     * 验证 API Key 列表 SQL 使用租户和应用编码隔离。
     */
    @Test
    void shouldGenerateSelectSqlWithTenantAndAppIsolation() {
        OpenApiApiKeySqlProvider provider = new OpenApiApiKeySqlProvider();

        String sql = provider.selectByTenantIdAndAppCode();

        assertTrue(sql.contains("FROM openapi_api_key"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND app_code = #{appCode}"));
        assertTrue(sql.contains("ORDER BY access_key"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证 API Key 保存 SQL 通过访问密钥唯一键做新增或更新。
     */
    @Test
    void shouldGenerateUpsertSql() {
        OpenApiApiKeySqlProvider provider = new OpenApiApiKeySqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO openapi_api_key"));
        assertTrue(sql.contains("access_key"));
        assertTrue(sql.contains("secret_cipher"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
