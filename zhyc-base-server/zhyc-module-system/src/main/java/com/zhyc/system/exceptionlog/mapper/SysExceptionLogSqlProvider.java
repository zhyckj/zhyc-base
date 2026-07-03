/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog.mapper;

/**
 * 系统异常日志 SQL Provider。
 */
public class SysExceptionLogSqlProvider {

    /**
     * 生成系统异常日志新增 SQL。
     *
     * @return 系统异常日志新增 SQL
     */
    public String insert() {
        return """
            INSERT INTO sys_exception_log (
                tenant_id,
                trace_id,
                user_id,
                username,
                request_uri,
                request_method,
                exception_name,
                message,
                stack_trace,
                client_ip,
                created_at
            ) VALUES (
                #{tenantId},
                #{traceId},
                #{userId},
                #{username},
                #{requestUri},
                #{requestMethod},
                #{exceptionName},
                #{message},
                #{stackTrace},
                #{clientIp},
                #{createdAt}
            )
            """;
    }

    /**
     * 生成租户最近异常日志查询 SQL。
     *
     * @return 租户最近异常日志查询 SQL
     */
    public String selectRecentByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   trace_id AS traceId,
                   user_id AS userId,
                   username,
                   request_uri AS requestUri,
                   request_method AS requestMethod,
                   exception_name AS exceptionName,
                   message,
                   stack_trace AS stackTrace,
                   client_ip AS clientIp,
                   created_at AS createdAt
            FROM sys_exception_log
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """;
    }
}
