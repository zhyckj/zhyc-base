/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature.mapper;

/**
 * 开放 API 签名策略 SQL Provider。
 */
public class OpenApiSignaturePolicySqlProvider {

    /**
     * 生成租户指定应用签名策略列表查询 SQL。
     *
     * @return 签名策略列表查询 SQL
     */
    public String selectByTenantIdAndAppCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   app_code AS appCode,
                   algorithm,
                   timestamp_tolerance_seconds AS timestampToleranceSeconds,
                   nonce_ttl_seconds AS nonceTtlSeconds,
                   require_body_hash AS requireBodyHash,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM openapi_signature_policy
            WHERE tenant_id = #{tenantId}
              AND app_code = #{appCode}
            ORDER BY app_code
            """;
    }

    /**
     * 生成签名策略新增或更新 SQL。
     *
     * @return 签名策略新增或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO openapi_signature_policy (
                tenant_id, app_code, algorithm, timestamp_tolerance_seconds,
                nonce_ttl_seconds, require_body_hash, status
            ) VALUES (
                #{tenantId}, #{appCode}, #{algorithm}, #{timestampToleranceSeconds},
                #{nonceTtlSeconds}, #{requireBodyHash}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                algorithm = VALUES(algorithm),
                timestamp_tolerance_seconds = VALUES(timestamp_tolerance_seconds),
                nonce_ttl_seconds = VALUES(nonce_ttl_seconds),
                require_body_hash = VALUES(require_body_hash),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
