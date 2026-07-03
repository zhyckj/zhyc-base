/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.mapper;

/**
 * AI 调用审计 SQL Provider。
 */
public class AiInvocationAuditSqlProvider {

    public String selectByTenantIdAndAppCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   provider_id AS providerId,
                   model_id AS modelId,
                   invocation_type AS invocationType,
                   prompt_tokens AS promptTokens,
                   completion_tokens AS completionTokens,
                   total_tokens AS totalTokens,
                   latency_ms AS latencyMs,
                   status,
                   error_message AS errorMessage,
                   trace_id AS traceId,
                   created_at AS createdAt
            FROM ai_invocation_audit
            WHERE tenant_id = #{tenantId}
              AND app_code = #{appCode}
            ORDER BY created_at DESC
            LIMIT 200
            """;
    }

    public String insert() {
        return """
            INSERT INTO ai_invocation_audit (
                tenant_id, app_code, provider_id, model_id, invocation_type, prompt_tokens,
                completion_tokens, total_tokens, latency_ms, status, error_message, trace_id
            ) VALUES (
                #{tenantId}, #{appCode}, #{providerId}, #{modelId}, #{invocationType}, #{promptTokens},
                #{completionTokens}, #{totalTokens}, #{latencyMs}, #{status}, #{errorMessage}, #{traceId}
            )
            """;
    }
}
