/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.mapper;

/**
 * API Key SQL Provider。
 */
public class OpenApiApiKeySqlProvider {

    /**
     * 生成租户指定应用 API Key 列表查询 SQL。
     *
     * @return API Key 列表查询 SQL
     */
    public String selectByTenantIdAndAppCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   access_key AS accessKey,
                   secret_cipher AS secretCipher,
                   status,
                   expire_at AS expireAt,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM openapi_api_key
            WHERE tenant_id = #{tenantId}
              AND app_code = #{appCode}
            ORDER BY access_key
            """;
    }

    /**
     * 生成 API Key 新增或更新 SQL。
     *
     * @return API Key 新增或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO openapi_api_key (
                tenant_id, app_code, access_key, secret_cipher, status, expire_at
            ) VALUES (
                #{tenantId}, #{appCode}, #{accessKey}, #{secretCipher}, #{status}, #{expireAt}
            )
            ON DUPLICATE KEY UPDATE
                tenant_id = VALUES(tenant_id),
                app_code = VALUES(app_code),
                secret_cipher = VALUES(secret_cipher),
                status = VALUES(status),
                expire_at = VALUES(expire_at),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
