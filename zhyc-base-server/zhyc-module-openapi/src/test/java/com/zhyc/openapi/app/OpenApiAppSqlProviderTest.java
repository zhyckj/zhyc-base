/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app;

import com.zhyc.openapi.app.mapper.OpenApiAppSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开发者应用 SQL Provider 测试。
 */
class OpenApiAppSqlProviderTest {

    /**
     * 验证开发者应用列表 SQL 使用显式列，并包含租户隔离条件。
     */
    @Test
    void shouldGenerateSelectSqlWithTenantIsolation() {
        OpenApiAppSqlProvider provider = new OpenApiAppSqlProvider();

        String sql = provider.selectByTenantId();

        assertTrue(sql.contains("FROM openapi_app"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("ORDER BY app_code"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证开发者应用精确查询 SQL 同时使用租户和应用编码，供子资源保存前校验应用归属。
     */
    @Test
    void shouldGenerateSelectByTenantAndAppCodeSql() {
        OpenApiAppSqlProvider provider = new OpenApiAppSqlProvider();

        String sql = provider.selectByTenantIdAndAppCode();

        assertTrue(sql.contains("FROM openapi_app"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND app_code = #{appCode}"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证开发者应用保存 SQL 通过租户和应用编码唯一键做新增或更新。
     */
    @Test
    void shouldGenerateUpsertSql() {
        OpenApiAppSqlProvider provider = new OpenApiAppSqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO openapi_app"));
        assertTrue(sql.contains("app_code"));
        assertTrue(sql.contains("auth_mode"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
