/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.mapper;

/**
 * 开发者应用 SQL Provider。
 */
public class OpenApiAppSqlProvider {

    /**
     * 生成租户开发者应用列表查询 SQL。
     *
     * @return 开发者应用列表查询 SQL
     */
    public String selectByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   app_name AS appName,
                   owner_user_id AS ownerUserId,
                   auth_mode AS authMode,
                   ip_whitelist AS ipWhitelist,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM openapi_app
            WHERE tenant_id = #{tenantId}
            ORDER BY app_code
            """;
    }

    /**
     * 生成按租户和应用编码查询开发者应用 SQL。
     *
     * @return 开发者应用精确查询 SQL
     */
    public String selectByTenantIdAndAppCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   app_name AS appName,
                   owner_user_id AS ownerUserId,
                   auth_mode AS authMode,
                   ip_whitelist AS ipWhitelist,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM openapi_app
            WHERE tenant_id = #{tenantId}
              AND app_code = #{appCode}
            """;
    }

    /**
     * 生成开发者应用新增或更新 SQL。
     *
     * @return 开发者应用新增或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO openapi_app (
                tenant_id, app_code, app_name, owner_user_id, auth_mode, ip_whitelist, status
            ) VALUES (
                #{tenantId}, #{appCode}, #{appName}, #{ownerUserId}, #{authMode}, #{ipWhitelist}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                app_name = VALUES(app_name),
                owner_user_id = VALUES(owner_user_id),
                auth_mode = VALUES(auth_mode),
                ip_whitelist = VALUES(ip_whitelist),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
