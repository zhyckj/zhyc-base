/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.mapper;

/**
 * AI 提示词模板 SQL Provider。
 */
public class AiPromptTemplateSqlProvider {

    public String selectByTenantId() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
            ORDER BY prompt_code, version
            """;
    }

    public String selectByTenantIdAndPromptCodeAndVersion() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
              AND prompt_code = #{promptCode}
              AND version = #{version}
            """;
    }

    public String upsert() {
        return """
            INSERT INTO ai_prompt_template (
                tenant_id, prompt_code, prompt_name, version, template_content, variables, status
            ) VALUES (
                #{tenantId}, #{promptCode}, #{promptName}, #{version}, #{templateContent}, #{variables}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                prompt_name = VALUES(prompt_name),
                template_content = VALUES(template_content),
                variables = VALUES(variables),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }

    private String baseSelect() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   prompt_code AS promptCode,
                   prompt_name AS promptName,
                   version,
                   template_content AS templateContent,
                   variables,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM ai_prompt_template
            """;
    }
}
