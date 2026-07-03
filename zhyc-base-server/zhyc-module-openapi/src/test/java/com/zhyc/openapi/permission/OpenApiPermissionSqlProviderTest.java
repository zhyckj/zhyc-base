/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission;

import com.zhyc.openapi.permission.mapper.OpenApiPermissionSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 权限授权 SQL Provider 测试。
 */
class OpenApiPermissionSqlProviderTest {

    /**
     * 验证开放 API 权限列表 SQL 使用租户和应用编码隔离。
     */
    @Test
    void shouldGenerateSelectSqlWithTenantAndAppIsolation() {
        OpenApiPermissionSqlProvider provider = new OpenApiPermissionSqlProvider();

        String sql = provider.selectByTenantIdAndAppCode();

        assertTrue(sql.contains("FROM openapi_api_permission"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND app_code = #{appCode}"));
        assertTrue(sql.contains("ORDER BY api_code"));
        assertFalse(sql.contains("SELECT *"));
    }

    /**
     * 验证开放 API 权限保存 SQL 通过租户、应用和 API 编码唯一键做新增或更新。
     */
    @Test
    void shouldGenerateUpsertSql() {
        OpenApiPermissionSqlProvider provider = new OpenApiPermissionSqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO openapi_api_permission"));
        assertTrue(sql.contains("api_code"));
        assertTrue(sql.contains("path_pattern"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
