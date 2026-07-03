/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog.mapper;

/**
 * 开放 API 目录 SQL Provider。
 */
public class OpenApiCatalogSqlProvider {

    /**
     * 生成按分组查询 API 目录列表 SQL。
     *
     * @return API 目录列表查询 SQL
     */
    public String selectByGroupCode() {
        return """
            SELECT id,
                   api_code AS apiCode,
                   api_name AS apiName,
                   group_code AS groupCode,
                   http_method AS httpMethod,
                   path_pattern AS pathPattern,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM openapi_catalog
            WHERE group_code = #{groupCode}
            ORDER BY group_code, api_code
            """;
    }

    /**
     * 生成 API 目录新增或更新 SQL。
     *
     * @return API 目录新增或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO openapi_catalog (
                api_code, api_name, group_code, http_method, path_pattern, status
            ) VALUES (
                #{apiCode}, #{apiName}, #{groupCode}, #{httpMethod}, #{pathPattern}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                api_name = VALUES(api_name),
                group_code = VALUES(group_code),
                http_method = VALUES(http_method),
                path_pattern = VALUES(path_pattern),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
