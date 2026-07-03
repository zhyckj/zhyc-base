/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.mapper;

/**
 * AI 模型供应商 SQL Provider。
 */
public class AiProviderSqlProvider {

    public String selectByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   provider_code AS providerCode,
                   provider_name AS providerName,
                   provider_type AS providerType,
                   base_url AS baseUrl,
                   secret_ref AS secretRef,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM ai_provider
            WHERE tenant_id = #{tenantId}
            ORDER BY provider_code
            """;
    }

    public String selectByTenantIdAndProviderCode() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
              AND provider_code = #{providerCode}
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
            INSERT INTO ai_provider (
                tenant_id, provider_code, provider_name, provider_type, base_url, secret_ref, status
            ) VALUES (
                #{tenantId}, #{providerCode}, #{providerName}, #{providerType}, #{baseUrl}, #{secretRef}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                provider_name = VALUES(provider_name),
                provider_type = VALUES(provider_type),
                base_url = VALUES(base_url),
                secret_ref = VALUES(secret_ref),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }

    private String baseSelect() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   provider_code AS providerCode,
                   provider_name AS providerName,
                   provider_type AS providerType,
                   base_url AS baseUrl,
                   secret_ref AS secretRef,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM ai_provider
            """;
    }
}
