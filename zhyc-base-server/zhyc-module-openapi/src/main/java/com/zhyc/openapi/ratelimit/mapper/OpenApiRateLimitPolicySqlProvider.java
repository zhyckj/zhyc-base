/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit.mapper;

/**
 * 开放 API 限流策略 SQL Provider。
 */
public class OpenApiRateLimitPolicySqlProvider {

    /**
     * 生成租户指定应用限流策略列表查询 SQL。
     *
     * @return 限流策略列表查询 SQL
     */
    public String selectByTenantIdAndAppCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   api_code AS apiCode,
                   limit_count AS limitCount,
                   window_seconds AS windowSeconds,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM openapi_rate_limit_policy
            WHERE tenant_id = #{tenantId}
              AND app_code = #{appCode}
            ORDER BY api_code
            """;
    }

    /**
     * 生成限流策略新增或更新 SQL。
     *
     * @return 限流策略新增或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO openapi_rate_limit_policy (
                tenant_id, app_code, api_code, limit_count, window_seconds, status
            ) VALUES (
                #{tenantId}, #{appCode}, #{apiCode}, #{limitCount}, #{windowSeconds}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                limit_count = VALUES(limit_count),
                window_seconds = VALUES(window_seconds),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
