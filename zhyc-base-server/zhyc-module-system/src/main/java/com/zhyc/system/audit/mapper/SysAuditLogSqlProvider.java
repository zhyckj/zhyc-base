/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit.mapper;

/**
 * 系统审计日志 SQL Provider。
 */
public class SysAuditLogSqlProvider {

    /**
     * 生成系统审计日志新增 SQL。
     *
     * @return 系统审计日志新增 SQL
     */
    public String insert() {
        return """
            INSERT INTO sys_audit_log (
                tenant_id,
                user_id,
                username,
                action,
                target_type,
                target_id,
                result,
                client_ip,
                detail,
                created_at
            ) VALUES (
                #{tenantId},
                #{userId},
                #{username},
                #{action},
                #{targetType},
                #{targetId},
                #{result},
                #{clientIp},
                #{detail},
                #{createdAt}
            )
            """;
    }

    /**
     * 生成租户最近审计日志查询 SQL。
     *
     * @return 租户最近审计日志查询 SQL
     */
    public String selectRecentByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   user_id AS userId,
                   username,
                   action,
                   target_type AS targetType,
                   target_id AS targetId,
                   result,
                   client_ip AS clientIp,
                   detail,
                   created_at AS createdAt
            FROM sys_audit_log
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """;
    }
}
