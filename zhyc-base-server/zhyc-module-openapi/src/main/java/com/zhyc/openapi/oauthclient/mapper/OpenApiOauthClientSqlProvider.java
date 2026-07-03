/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.mapper;

/**
 * 开放平台 OAuth2 客户端映射 SQL Provider。
 */
public class OpenApiOauthClientSqlProvider {

    /**
     * 生成租户指定应用 OAuth2 客户端映射列表查询 SQL。
     *
     * @return OAuth2 客户端映射列表查询 SQL
     */
    public String selectByTenantIdAndAppCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   client_id AS clientId,
                   allowed_scopes AS allowedScopes,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM openapi_oauth_client
            WHERE tenant_id = #{tenantId}
              AND app_code = #{appCode}
            ORDER BY client_id
            """;
    }

    /**
     * 生成 OAuth2 客户端映射新增或更新 SQL。
     *
     * @return OAuth2 客户端映射新增或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO openapi_oauth_client (
                tenant_id, app_code, client_id, allowed_scopes, status
            ) VALUES (
                #{tenantId}, #{appCode}, #{clientId}, #{allowedScopes}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                allowed_scopes = VALUES(allowed_scopes),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
