/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.mapper;

import java.util.Map;

/**
 * 系统密钥 SQL Provider。
 */
public class SysSecretSqlProvider {

    /**
     * 生成当前租户密钥列表查询 SQL。
     *
     * @return 密钥列表查询 SQL
     */
    public String selectByTenantId() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
            ORDER BY secret_code, id
            """;
    }

    /**
     * 生成按租户和主键查询密钥 SQL。
     *
     * @return 密钥查询 SQL
     */
    public String selectByTenantIdAndId() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
            """;
    }

    /**
     * 生成按租户和密钥编码查询密钥 SQL。
     *
     * @return 密钥查询 SQL
     */
    public String selectByTenantIdAndSecretCode() {
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
              AND secret_code = #{secretCode}
            """;
    }

    /**
     * 生成当前租户可用于下拉选择的密钥查询 SQL。
     *
     * <p>指定密钥类型时按类型精确过滤；未指定类型时只返回数据源兼容的数据库口令和通用密钥。</p>
     *
     * @param params MyBatis 查询参数
     * @return 密钥选项查询 SQL
     */
    public String selectSelectableSecrets(Map<String, Object> params) {
        Object secretKind = params == null ? null : params.get("secretKind");
        String secretKindCondition = secretKind == null || secretKind.toString().trim().isEmpty()
                ? "  AND secret_kind IN ('db_password', 'generic')\n"
                : "  AND secret_kind = #{secretKind}\n";
        return baseSelect() + """
            WHERE tenant_id = #{tenantId}
              AND status = #{status}
            """ + secretKindCondition + """
            ORDER BY secret_code, id
            """;
    }

    /**
     * 生成新增系统密钥 SQL。
     *
     * @return 新增 SQL
     */
    public String insert() {
        return """
            INSERT INTO sys_secret (
                tenant_id, secret_code, secret_name, secret_kind, secret_cipher,
                secret_mask, status, expire_at, last_rotated_at
            ) VALUES (
                #{tenantId}, #{secretCode}, #{secretName}, #{secretKind}, #{secretCipher},
                #{secretMask}, #{status}, #{expireAt}, #{lastRotatedAt}
            )
            """;
    }

    /**
     * 生成更新系统密钥 SQL。
     *
     * @return 更新 SQL
     */
    public String update() {
        return """
            UPDATE sys_secret
            SET secret_code = #{secretCode},
                secret_name = #{secretName},
                secret_kind = #{secretKind},
                secret_cipher = #{secretCipher},
                secret_mask = #{secretMask},
                status = #{status},
                expire_at = #{expireAt},
                last_rotated_at = #{lastRotatedAt},
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
            """;
    }

    /**
     * 生成删除系统密钥 SQL。
     *
     * @return 删除 SQL
     */
    public String deleteByTenantIdAndId() {
        return """
            DELETE FROM sys_secret
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
            """;
    }

    private String baseSelect() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   secret_code AS secretCode,
                   secret_name AS secretName,
                   secret_kind AS secretKind,
                   secret_cipher AS secretCipher,
                   secret_mask AS secretMask,
                   status,
                   expire_at AS expireAt,
                   last_rotated_at AS lastRotatedAt,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_secret
            """;
    }
}
