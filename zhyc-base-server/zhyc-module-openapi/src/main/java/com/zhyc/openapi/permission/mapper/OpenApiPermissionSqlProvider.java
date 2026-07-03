/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission.mapper;

/**
 * 开放 API 权限授权 SQL Provider。
 */
public class OpenApiPermissionSqlProvider {

    /**
     * 生成租户指定应用开放 API 授权列表查询 SQL。
     *
     * @return 开放 API 授权列表查询 SQL
     */
    public String selectByTenantIdAndAppCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   api_code AS apiCode,
                   api_name AS apiName,
                   http_method AS httpMethod,
                   path_pattern AS pathPattern,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM openapi_api_permission
            WHERE tenant_id = #{tenantId}
              AND app_code = #{appCode}
            ORDER BY api_code
            """;
    }

    /**
     * 生成开放 API 授权新增或更新 SQL。
     *
     * @return 开放 API 授权新增或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO openapi_api_permission (
                tenant_id, app_code, api_code, api_name, http_method, path_pattern, status
            ) VALUES (
                #{tenantId}, #{appCode}, #{apiCode}, #{apiName}, #{httpMethod}, #{pathPattern}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                api_name = VALUES(api_name),
                http_method = VALUES(http_method),
                path_pattern = VALUES(path_pattern),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
