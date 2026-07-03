/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.mapper;

/**
 * 系统编码规则 SQL Provider。
 */
public class SysCodeRuleSqlProvider {

    /**
     * 生成租户编码规则列表查询 SQL。
     *
     * @return 编码规则列表查询 SQL
     */
    public String selectByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   rule_code AS ruleCode,
                   rule_name AS ruleName,
                   prefix,
                   date_pattern AS datePattern,
                   sequence_length AS sequenceLength,
                   current_value AS currentValue,
                   enabled,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_code_rule
            WHERE tenant_id = #{tenantId}
            ORDER BY rule_code
            """;
    }

    /**
     * 生成按编码规则编码查询租户编码规则 SQL。
     *
     * @return 按编码规则编码查询 SQL
     */
    public String selectByTenantIdAndRuleCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   rule_code AS ruleCode,
                   rule_name AS ruleName,
                   prefix,
                   date_pattern AS datePattern,
                   sequence_length AS sequenceLength,
                   current_value AS currentValue,
                   enabled,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_code_rule
            WHERE tenant_id = #{tenantId}
              AND rule_code = #{ruleCode}
            """;
    }

    /**
     * 生成编码规则新增或更新 SQL。
     *
     * @return 编码规则新增或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO sys_code_rule (
                tenant_id, rule_code, rule_name, prefix, date_pattern, sequence_length, current_value, enabled
            ) VALUES (
                #{tenantId}, #{ruleCode}, #{ruleName}, #{prefix}, #{datePattern}, #{sequenceLength},
                #{currentValue}, #{enabled}
            )
            ON DUPLICATE KEY UPDATE
                rule_name = VALUES(rule_name),
                prefix = VALUES(prefix),
                date_pattern = VALUES(date_pattern),
                sequence_length = VALUES(sequence_length),
                current_value = VALUES(current_value),
                enabled = VALUES(enabled),
                updated_at = CURRENT_TIMESTAMP
            """;
    }

    /**
     * 生成编码规则当前序列值更新 SQL。
     *
     * @return 编码规则当前序列值更新 SQL
     */
    public String updateCurrentValue() {
        return """
            UPDATE sys_code_rule
            SET current_value = #{currentValue},
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId}
              AND rule_code = #{ruleCode}
            """;
    }
}
