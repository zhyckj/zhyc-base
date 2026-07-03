/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient;

import com.zhyc.openapi.oauthclient.mapper.OpenApiOauthClientSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放平台 OAuth2 客户端映射 SQL Provider 测试。
 */
class OpenApiOauthClientSqlProviderTest {

    /**
     * 验证 OAuth2 客户端映射列表 SQL 使用租户和应用编码隔离。
     */
    @Test
    void shouldSelectOauthClientsByTenantAndApp() {
        OpenApiOauthClientSqlProvider provider = new OpenApiOauthClientSqlProvider();

        String sql = provider.selectByTenantIdAndAppCode();

        assertTrue(sql.contains("FROM openapi_oauth_client"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND app_code = #{appCode}"));
        assertTrue(sql.contains("ORDER BY client_id"));
    }

    /**
     * 验证 OAuth2 客户端映射保存 SQL 使用唯一键更新。
     */
    @Test
    void shouldUpsertOauthClient() {
        OpenApiOauthClientSqlProvider provider = new OpenApiOauthClientSqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO openapi_oauth_client"));
        assertTrue(sql.contains("tenant_id, app_code, client_id, allowed_scopes, status"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
