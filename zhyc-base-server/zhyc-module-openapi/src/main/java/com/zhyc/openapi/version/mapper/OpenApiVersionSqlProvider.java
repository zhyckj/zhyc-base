/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version.mapper;

/**
 * 开放 API 版本发布 SQL Provider。
 */
public class OpenApiVersionSqlProvider {

    /**
     * 生成按 API 编码查询版本列表 SQL。
     *
     * @return API 版本列表查询 SQL
     */
    public String selectByApiCode() {
        return """
            SELECT id,
                   api_code AS apiCode,
                   version,
                   backend_route AS backendRoute,
                   request_schema AS requestSchema,
                   response_schema AS responseSchema,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM openapi_version
            WHERE api_code = #{apiCode}
            ORDER BY version
            """;
    }

    /**
     * 生成 API 版本发布或更新 SQL。
     *
     * @return API 版本发布或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO openapi_version (
                api_code, version, backend_route, request_schema, response_schema, status
            ) VALUES (
                #{apiCode}, #{version}, #{backendRoute}, #{requestSchema}, #{responseSchema}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                backend_route = VALUES(backend_route),
                request_schema = VALUES(request_schema),
                response_schema = VALUES(response_schema),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
