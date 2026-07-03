/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.mapper;

/**
 * AI 应用接入 SQL Provider。
 */
public class AiAppSqlProvider {

    public String selectByTenantId() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
            ORDER BY app_code
            """;
    }

    public String selectByTenantIdAndAppCode() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
              AND app_code = #{appCode}
            """;
    }

    public String upsert() {
        return """
            INSERT INTO ai_app (
                tenant_id, app_code, app_name, default_model_id, system_prompt, daily_token_quota, status
            ) VALUES (
                #{tenantId}, #{appCode}, #{appName}, #{defaultModelId}, #{systemPrompt}, #{dailyTokenQuota}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                app_name = VALUES(app_name),
                default_model_id = VALUES(default_model_id),
                system_prompt = VALUES(system_prompt),
                daily_token_quota = VALUES(daily_token_quota),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }

    private String baseSelect() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   app_name AS appName,
                   default_model_id AS defaultModelId,
                   system_prompt AS systemPrompt,
                   daily_token_quota AS dailyTokenQuota,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM ai_app
            """;
    }
}
