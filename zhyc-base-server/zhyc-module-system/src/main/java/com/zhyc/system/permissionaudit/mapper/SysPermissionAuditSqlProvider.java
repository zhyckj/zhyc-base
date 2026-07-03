/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit.mapper;

/**
 * 系统权限变更审计 SQL Provider。
 */
public class SysPermissionAuditSqlProvider {

    /**
     * 生成系统权限变更审计新增 SQL。
     *
     * @return 系统权限变更审计新增 SQL
     */
    public String insert() {
        return """
            INSERT INTO sys_permission_audit (
                tenant_id,
                operator_id,
                target_type,
                target_id,
                before_value,
                after_value,
                change_type,
                created_at
            ) VALUES (
                #{tenantId},
                #{operatorId},
                #{targetType},
                #{targetId},
                #{beforeValue},
                #{afterValue},
                #{changeType},
                #{createdAt}
            )
            """;
    }

    /**
     * 生成租户最近权限变更审计查询 SQL。
     *
     * @return 租户最近权限变更审计查询 SQL
     */
    public String selectRecentByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   operator_id AS operatorId,
                   target_type AS targetType,
                   target_id AS targetId,
                   before_value AS beforeValue,
                   after_value AS afterValue,
                   change_type AS changeType,
                   created_at AS createdAt
            FROM sys_permission_audit
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """;
    }
}
