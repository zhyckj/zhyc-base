/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam.mapper;

/**
 * 租户参数 SQL Provider。
 */
public class SysTenantParamSqlProvider {

    /**
     * 生成租户参数列表查询 SQL。
     *
     * @return 租户参数列表查询 SQL
     */
    public String selectByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   param_key AS paramKey,
                   param_value AS paramValue,
                   value_type AS valueType,
                   visible,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_tenant_param
            WHERE tenant_id = #{tenantId}
            ORDER BY param_key
            """;
    }

    /**
     * 生成按参数键查询租户参数 SQL。
     *
     * @return 按参数键查询租户参数 SQL
     */
    public String selectByTenantIdAndParamKey() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   param_key AS paramKey,
                   param_value AS paramValue,
                   value_type AS valueType,
                   visible,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_tenant_param
            WHERE tenant_id = #{tenantId}
              AND param_key = #{paramKey}
            """;
    }

    /**
     * 生成租户参数新增或更新 SQL。
     *
     * @return 租户参数新增或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO sys_tenant_param (
                tenant_id,
                param_key,
                param_value,
                value_type,
                visible
            ) VALUES (
                #{tenantId},
                #{paramKey},
                #{paramValue},
                #{valueType},
                #{visible}
            )
            ON DUPLICATE KEY UPDATE
                param_value = VALUES(param_value),
                value_type = VALUES(value_type),
                visible = VALUES(visible),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
