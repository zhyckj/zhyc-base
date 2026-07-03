/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.mapper;

/**
 * 系统访问限制 SQL Provider。
 */
public class SysAccessRestrictionSqlProvider {

    /**
     * 生成当前生效访问限制查询 SQL。
     *
     * @return 当前生效访问限制查询 SQL
     */
    public String selectActiveRestrictions() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   restriction_type AS restrictionType,
                   rule_value AS ruleValue,
                   effect,
                   start_at AS startAt,
                   end_at AS endAt,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_access_restriction
            WHERE tenant_id = #{tenantId}
              AND restriction_type = #{restrictionType}
              AND (start_at IS NULL OR start_at <= #{now})
              AND (end_at IS NULL OR end_at >= #{now})
            ORDER BY effect, rule_value
            """;
    }

    /**
     * 生成访问限制新增或更新 SQL。
     *
     * @return 访问限制新增或更新 SQL
     */
    public String saveRestriction() {
        return """
            INSERT INTO sys_access_restriction (
                tenant_id, restriction_type, rule_value, effect, start_at, end_at
            ) VALUES (
                #{tenantId}, #{restrictionType}, #{ruleValue}, #{effect}, #{startAt}, #{endAt}
            )
            ON DUPLICATE KEY UPDATE
                effect = VALUES(effect),
                start_at = VALUES(start_at),
                end_at = VALUES(end_at),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
