/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature;

import com.zhyc.openapi.signature.mapper.OpenApiSignaturePolicySqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 签名策略 SQL Provider 测试。
 */
class OpenApiSignaturePolicySqlProviderTest {

    /**
     * 验证签名策略列表 SQL 使用租户和应用编码隔离。
     */
    @Test
    void shouldSelectPoliciesByTenantAndApp() {
        OpenApiSignaturePolicySqlProvider provider = new OpenApiSignaturePolicySqlProvider();

        String sql = provider.selectByTenantIdAndAppCode();

        assertTrue(sql.contains("FROM openapi_signature_policy"));
        assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
        assertTrue(sql.contains("AND app_code = #{appCode}"));
        assertTrue(sql.contains("ORDER BY app_code"));
    }

    /**
     * 验证保存签名策略 SQL 使用唯一键更新。
     */
    @Test
    void shouldUpsertPolicy() {
        OpenApiSignaturePolicySqlProvider provider = new OpenApiSignaturePolicySqlProvider();

        String sql = provider.upsert();

        assertTrue(sql.contains("INSERT INTO openapi_signature_policy"));
        assertTrue(sql.contains("tenant_id, app_code, algorithm, timestamp_tolerance_seconds"));
        assertTrue(sql.contains("nonce_ttl_seconds, require_body_hash, status"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    }
}
