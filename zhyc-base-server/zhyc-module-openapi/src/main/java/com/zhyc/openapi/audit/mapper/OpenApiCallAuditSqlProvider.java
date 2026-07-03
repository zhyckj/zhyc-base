/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit.mapper;

/**
 * 开放 API 调用审计 SQL Provider。
 */
public class OpenApiCallAuditSqlProvider {

    /**
     * 生成租户指定应用开放 API 调用审计列表查询 SQL。
     *
     * @return 开放 API 调用审计列表查询 SQL
     */
    public String selectByTenantIdAndAppCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   access_key AS accessKey,
                   api_code AS apiCode,
                   http_method AS httpMethod,
                   request_path AS requestPath,
                   response_status AS responseStatus,
                   duration_ms AS durationMs,
                   success,
                   error_code AS errorCode,
                   client_ip AS clientIp,
                   request_id AS requestId,
                   called_at AS calledAt,
                   created_at AS createdAt
            FROM openapi_call_audit
            WHERE tenant_id = #{tenantId}
              AND app_code = #{appCode}
            ORDER BY called_at DESC
            """;
    }

    /**
     * 生成租户指定应用开放 API 错误日志列表查询 SQL。
     *
     * @return 开放 API 错误日志列表查询 SQL
     */
    public String selectErrorLogsByTenantIdAndAppCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   access_key AS accessKey,
                   api_code AS apiCode,
                   http_method AS httpMethod,
                   request_path AS requestPath,
                   response_status AS responseStatus,
                   duration_ms AS durationMs,
                   success,
                   error_code AS errorCode,
                   client_ip AS clientIp,
                   request_id AS requestId,
                   called_at AS calledAt,
                   created_at AS createdAt
            FROM openapi_call_audit
            WHERE tenant_id = #{tenantId}
              AND app_code = #{appCode}
              AND success = 0
            ORDER BY called_at DESC
            """;
    }

    /**
     * 生成开放 API 调用审计写入 SQL。
     *
     * @return 开放 API 调用审计写入 SQL
     */
    public String insert() {
        return """
            INSERT INTO openapi_call_audit (
                tenant_id, app_code, access_key, api_code, http_method, request_path,
                response_status, duration_ms, success, error_code, client_ip, request_id, called_at
            ) VALUES (
                #{tenantId}, #{appCode}, #{accessKey}, #{apiCode}, #{httpMethod}, #{requestPath},
                #{responseStatus}, #{durationMs}, #{success}, #{errorCode}, #{clientIp}, #{requestId}, #{calledAt}
            )
            """;
    }
}
