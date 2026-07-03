/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog.mapper;

/**
 * 系统登录日志 SQL Provider。
 */
public class SysLoginLogSqlProvider {

    /**
     * 生成系统登录日志新增 SQL。
     *
     * @return 系统登录日志新增 SQL
     */
    public String insert() {
        return """
            INSERT INTO sys_login_log (
                tenant_id,
                user_id,
                username,
                login_type,
                result,
                client_ip,
                user_agent,
                created_at
            ) VALUES (
                #{tenantId},
                #{userId},
                #{username},
                #{loginType},
                #{result},
                #{clientIp},
                #{userAgent},
                #{createdAt}
            )
            """;
    }

    /**
     * 生成租户最近登录日志查询 SQL。
     *
     * @return 租户最近登录日志查询 SQL
     */
    public String selectRecentByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   user_id AS userId,
                   username,
                   login_type AS loginType,
                   result,
                   client_ip AS clientIp,
                   user_agent AS userAgent,
                   created_at AS createdAt
            FROM sys_login_log
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """;
    }
}
