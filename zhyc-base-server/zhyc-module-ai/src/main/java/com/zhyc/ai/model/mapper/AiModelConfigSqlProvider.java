/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.mapper;

/**
 * AI 模型配置 SQL Provider。
 */
public class AiModelConfigSqlProvider {

    public String selectByTenantId() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
            ORDER BY model_code
            """;
    }

    public String selectByTenantIdAndModelCode() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
              AND model_code = #{modelCode}
            """;
    }

    public String selectByTenantIdAndId() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
            """;
    }

    public String upsert() {
        return """
            INSERT INTO ai_model_config (
                tenant_id, provider_id, model_code, model_name, model_type, context_window,
                support_stream, support_tool, status
            ) VALUES (
                #{tenantId}, #{providerId}, #{modelCode}, #{modelName}, #{modelType}, #{contextWindow},
                #{supportStream}, #{supportTool}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                provider_id = VALUES(provider_id),
                model_name = VALUES(model_name),
                model_type = VALUES(model_type),
                context_window = VALUES(context_window),
                support_stream = VALUES(support_stream),
                support_tool = VALUES(support_tool),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }

    private String baseSelect() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   provider_id AS providerId,
                   model_code AS modelCode,
                   model_name AS modelName,
                   model_type AS modelType,
                   context_window AS contextWindow,
                   support_stream AS supportStream,
                   support_tool AS supportTool,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM ai_model_config
            """;
    }
}
