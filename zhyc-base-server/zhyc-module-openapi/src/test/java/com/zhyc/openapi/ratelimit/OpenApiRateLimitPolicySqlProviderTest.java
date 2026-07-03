/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit;

import com.zhyc.openapi.ratelimit.mapper.OpenApiRateLimitPolicySqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 限流策略 SQL Provider 测试。
 */
class OpenApiRateLimitPolicySqlProviderTest {

    /**
     * 验证限流策略列表 SQL 使用租户和应用编码隔离。
     */
    @Test
    void shouldSelectPoliciesByTenantAndApp() {
        OpenApiRateLimitPolicySqlProvider provider = new OpenApiRateLimitPolicySqlProvider();

        String sql = provider.selectByTenantIdAndAppCode();

        assertTrue(sql.contains("FROM openapi_rate_limit_policy"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND app_code = #{appCode}"));
        assertTrue(sql.contains("ORDER BY api_code"));
    }

    /**
     * 验证保存限流策略 SQL 使用唯一键更新。
     */
    @Test
    void shouldUpsertPolicy() {
        OpenApiRateLimitPolicySqlProvider provider = new OpenApiRateLimitPolicySqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO openapi_rate_limit_policy"));
        assertTrue(sql.contains("tenant_id, app_code, api_code, limit_count, window_seconds, status"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
