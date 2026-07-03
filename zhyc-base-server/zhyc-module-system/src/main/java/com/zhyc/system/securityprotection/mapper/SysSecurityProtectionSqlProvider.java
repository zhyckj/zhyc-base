/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.mapper;

/**
 * 系统安全防护中心 SQL Provider。
 */
public class SysSecurityProtectionSqlProvider {

    /**
     * 生成今日请求来源 IP 数量统计 SQL。
     *
     * @return 请求来源 IP 数量统计 SQL
     */
    public String countDistinctSourceIp() {
        return """
            SELECT COUNT(DISTINCT source_ip)
            FROM sys_security_event
            WHERE tenant_id = #{tenantId}
              AND occurred_at >= #{dayStart}
              AND occurred_at < #{dayEnd}
              AND source_ip IS NOT NULL
              AND source_ip <> ''
            """;
    }

    /**
     * 生成今日单 IP 最高请求次数统计 SQL。
     *
     * @return 单 IP 最高请求次数统计 SQL
     */
    public String maxIpRequestCount() {
        return """
            SELECT COALESCE(MAX(t.ip_count), 0)
            FROM (
                SELECT source_ip, COUNT(1) AS ip_count
                FROM sys_security_event
                WHERE tenant_id = #{tenantId}
                  AND occurred_at >= #{dayStart}
                  AND occurred_at < #{dayEnd}
                  AND source_ip IS NOT NULL
                  AND source_ip <> ''
                GROUP BY source_ip
            ) t
            """;
    }

    /**
     * 生成今日违规 IP 数量统计 SQL。
     *
     * @return 违规 IP 数量统计 SQL
     */
    public String countViolationIp() {
        return """
            SELECT COUNT(DISTINCT source_ip)
            FROM sys_security_event
            WHERE tenant_id = #{tenantId}
              AND occurred_at >= #{dayStart}
              AND occurred_at < #{dayEnd}
              AND source_ip IS NOT NULL
              AND source_ip <> ''
              AND (
                  action IN ('block', 'reject', 'captcha', 'rate_limit')
                  OR result IN ('blocked', 'denied', 'limited', 'failed')
                  OR event_level IN ('high', 'critical')
              )
            """;
    }

    /**
     * 生成当前有效封禁 IP 数量统计 SQL。
     *
     * @return 当前有效封禁 IP 数量统计 SQL
     */
    public String countActiveIpBlock() {
        return """
            SELECT COUNT(1)
            FROM sys_security_ip_block
            WHERE tenant_id = #{tenantId}
              AND status = 'active'
              AND deleted = 0
              AND (start_at IS NULL OR start_at <= #{now})
              AND (end_at IS NULL OR end_at >= #{now})
            """;
    }

    /**
     * 生成安全防护策略查询 SQL。
     *
     * @return 安全防护策略查询 SQL
     */
    public String selectPolicies() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   policy_code AS policyCode,
                   policy_name AS policyName,
                   protection_scope AS protectionScope,
                   target_pattern AS targetPattern,
                   threshold_limit AS thresholdLimit,
                   window_seconds AS windowSeconds,
                   action,
                   block_seconds AS blockSeconds,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_security_policy
            WHERE tenant_id = #{tenantId}
              AND deleted = 0
            ORDER BY protection_scope, sort_order, id
            """;
    }

    /**
     * 生成安全防护策略保存 SQL。
     *
     * @return 安全防护策略保存 SQL
     */
    public String savePolicy() {
        return """
            INSERT INTO sys_security_policy (
                tenant_id, policy_code, policy_name, protection_scope, target_pattern,
                threshold_limit, window_seconds, action, block_seconds, status
            ) VALUES (
                #{tenantId}, #{policyCode}, #{policyName}, #{protectionScope}, #{targetPattern},
                #{thresholdLimit}, #{windowSeconds}, #{action}, #{blockSeconds}, #{status}
            )
            ON DUPLICATE KEY UPDATE
                policy_name = VALUES(policy_name),
                protection_scope = VALUES(protection_scope),
                target_pattern = VALUES(target_pattern),
                threshold_limit = VALUES(threshold_limit),
                window_seconds = VALUES(window_seconds),
                action = VALUES(action),
                block_seconds = VALUES(block_seconds),
                status = VALUES(status),
                deleted = 0,
                updated_at = CURRENT_TIMESTAMP
            """;
    }

    /**
     * 生成安全事件写入 SQL。
     *
     * @return 安全事件写入 SQL
     */
    public String insertEvent() {
        return """
            INSERT INTO sys_security_event (
                tenant_id, event_type, event_level, source_ip, user_id, username,
                request_path, http_method, action, result, message, occurred_at
            ) VALUES (
                #{tenantId}, #{eventType}, #{eventLevel}, #{sourceIp}, #{userId}, #{username},
                #{requestPath}, #{httpMethod}, #{action}, #{result}, #{message}, #{occurredAt}
            )
            """;
    }

    /**
     * 生成最近安全事件查询 SQL。
     *
     * @return 最近安全事件查询 SQL
     */
    public String selectRecentEvents() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   event_type AS eventType,
                   event_level AS eventLevel,
                   source_ip AS sourceIp,
                   user_id AS userId,
                   username,
                   request_path AS requestPath,
                   http_method AS httpMethod,
                   action,
                   result,
                   message,
                   occurred_at AS occurredAt,
                   created_at AS createdAt
            FROM sys_security_event
            WHERE tenant_id = #{tenantId}
            ORDER BY occurred_at DESC, id DESC
            LIMIT #{limit}
            """;
    }

    /**
     * 生成来源 IP 请求排行 SQL。
     *
     * @return 来源 IP 请求排行 SQL
     */
    public String selectTopSourceIps() {
        return """
            SELECT source_ip AS name,
                   COUNT(1) AS requestCount
            FROM sys_security_event
            WHERE tenant_id = #{tenantId}
              AND occurred_at >= #{dayStart}
              AND occurred_at < #{dayEnd}
              AND source_ip IS NOT NULL
              AND source_ip <> ''
            GROUP BY source_ip
            ORDER BY requestCount DESC, source_ip ASC
            LIMIT #{limit}
            """;
    }

    /**
     * 生成接口访问排行 SQL。
     *
     * @return 接口访问排行 SQL
     */
    public String selectTopRequestPaths() {
        return """
            SELECT request_path AS name,
                   COUNT(1) AS requestCount
            FROM sys_security_event
            WHERE tenant_id = #{tenantId}
              AND occurred_at >= #{dayStart}
              AND occurred_at < #{dayEnd}
              AND request_path IS NOT NULL
              AND request_path <> ''
            GROUP BY request_path
            ORDER BY requestCount DESC, request_path ASC
            LIMIT #{limit}
            """;
    }

    /**
     * 生成 IP 封禁保存 SQL。
     *
     * @return IP 封禁保存 SQL
     */
    public String saveIpBlock() {
        return """
            INSERT INTO sys_security_ip_block (
                tenant_id, ip_value, block_type, reason, start_at, end_at, status, created_by
            ) VALUES (
                #{tenantId}, #{ipValue}, #{blockType}, #{reason}, #{startAt}, #{endAt}, #{status}, #{createdBy}
            )
            ON DUPLICATE KEY UPDATE
                block_type = VALUES(block_type),
                reason = VALUES(reason),
                start_at = VALUES(start_at),
                end_at = VALUES(end_at),
                status = VALUES(status),
                deleted = 0,
                updated_at = CURRENT_TIMESTAMP
            """;
    }

    /**
     * 生成拒绝访问限制同步 SQL。
     *
     * @return 拒绝访问限制同步 SQL
     */
    public String syncDenyAccessRestriction() {
        return """
            INSERT INTO sys_access_restriction (
                tenant_id, restriction_type, rule_value, effect, start_at, end_at
            ) VALUES (
                #{tenantId}, 'ip', #{ipValue}, 'deny', #{startAt}, #{endAt}
            )
            ON DUPLICATE KEY UPDATE
                effect = VALUES(effect),
                start_at = VALUES(start_at),
                end_at = VALUES(end_at),
                updated_at = CURRENT_TIMESTAMP
            """;
    }

    /**
     * 生成 IP 封禁解除 SQL。
     *
     * @return IP 封禁解除 SQL
     */
    public String deactivateIpBlock() {
        return """
            UPDATE sys_security_ip_block
            SET status = 'released',
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId}
              AND ip_value = #{ipValue}
              AND status = 'active'
              AND deleted = 0
            """;
    }

    /**
     * 生成同步拒绝访问限制解除 SQL。
     *
     * @return 同步拒绝访问限制解除 SQL
     */
    public String deactivateDenyAccessRestriction() {
        return """
            UPDATE sys_access_restriction
            SET end_at = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId}
              AND restriction_type = 'ip'
              AND rule_value = #{ipValue}
              AND effect = 'deny'
              AND (end_at IS NULL OR end_at >= CURRENT_TIMESTAMP)
            """;
    }

    /**
     * 生成有效 IP 封禁规则查询 SQL。
     *
     * @return 有效 IP 封禁规则查询 SQL
     */
    public String selectActiveIpBlockRules() {
        return """
            SELECT ip_value
            FROM sys_security_ip_block
            WHERE tenant_id = #{tenantId}
              AND status = 'active'
              AND deleted = 0
              AND (start_at IS NULL OR start_at <= #{now})
              AND (end_at IS NULL OR end_at >= #{now})
            ORDER BY updated_at DESC, id DESC
            """;
    }

    /**
     * 生成指定 IP 有效精确封禁统计 SQL。
     *
     * @return 指定 IP 有效精确封禁统计 SQL
     */
    public String countActiveIpBlockByValue() {
        return """
            SELECT COUNT(1)
            FROM sys_security_ip_block
            WHERE tenant_id = #{tenantId}
              AND ip_value = #{ipValue}
              AND status = 'active'
              AND deleted = 0
              AND (start_at IS NULL OR start_at <= #{now})
              AND (end_at IS NULL OR end_at >= #{now})
            """;
    }
}
