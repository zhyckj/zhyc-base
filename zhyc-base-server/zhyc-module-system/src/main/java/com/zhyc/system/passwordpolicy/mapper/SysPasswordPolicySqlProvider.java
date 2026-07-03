/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.mapper;

/**
 * 系统密码策略 SQL Provider。
 */
public class SysPasswordPolicySqlProvider {

    /**
     * 生成租户默认密码策略查询 SQL。
     *
     * @return 默认密码策略查询 SQL
     */
    public String selectDefaultByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   policy_code AS policyCode,
                   policy_name AS policyName,
                   min_length AS minLength,
                   require_uppercase AS requireUppercase,
                   require_lowercase AS requireLowercase,
                   require_digit AS requireDigit,
                   require_special AS requireSpecial,
                   expire_days AS expireDays,
                   history_count AS historyCount,
                   max_retry_count AS maxRetryCount,
                   lock_minutes AS lockMinutes,
                   enabled,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_password_policy
            WHERE tenant_id = #{tenantId}
              AND policy_code = 'default'
            """;
    }

    /**
     * 生成密码策略新增或更新 SQL。
     *
     * @return 密码策略新增或更新 SQL
     */
    public String upsert() {
        return """
            INSERT INTO sys_password_policy (
                tenant_id, policy_code, policy_name, min_length, require_uppercase, require_lowercase,
                require_digit, require_special, expire_days, history_count, max_retry_count, lock_minutes, enabled
            ) VALUES (
                #{tenantId}, #{policyCode}, #{policyName}, #{minLength}, #{requireUppercase},
                #{requireLowercase}, #{requireDigit}, #{requireSpecial}, #{expireDays}, #{historyCount},
                #{maxRetryCount}, #{lockMinutes}, #{enabled}
            )
            ON DUPLICATE KEY UPDATE
                policy_name = VALUES(policy_name),
                min_length = VALUES(min_length),
                require_uppercase = VALUES(require_uppercase),
                require_lowercase = VALUES(require_lowercase),
                require_digit = VALUES(require_digit),
                require_special = VALUES(require_special),
                expire_days = VALUES(expire_days),
                history_count = VALUES(history_count),
                max_retry_count = VALUES(max_retry_count),
                lock_minutes = VALUES(lock_minutes),
                enabled = VALUES(enabled),
                updated_at = CURRENT_TIMESTAMP
            """;
    }
}
