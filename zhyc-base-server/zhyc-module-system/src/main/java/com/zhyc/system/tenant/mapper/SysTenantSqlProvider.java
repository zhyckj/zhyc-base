/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.mapper;

/**
 * 系统租户 SQL Provider。
 */
public class SysTenantSqlProvider {

    /**
     * 生成按状态查询租户列表 SQL。
     *
     * @return 租户列表查询 SQL
     */
    public String selectByStatus() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   tenant_name AS name,
                   package_id AS packageId,
                   isolation_mode AS isolationMode,
                   status,
                   contact_name AS contactName,
                   contact_phone AS contactPhone,
                   expire_at AS expireAt,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_tenant
            WHERE status = #{status}
            ORDER BY tenant_id
            """;
    }

    /**
     * 生成按登录账号查询授权租户列表 SQL。
     *
     * <p>首期以租户内启用用户账号作为授权依据，只返回启用租户，避免移动端切换到无效或未授权租户。</p>
     *
     * @return 授权租户列表查询 SQL
     */
    public String selectAuthorizedByUsername() {
        return """
            SELECT t.id,
                   t.tenant_id AS tenantId,
                   t.tenant_name AS name,
                   t.package_id AS packageId,
                   t.isolation_mode AS isolationMode,
                   t.status,
                   t.contact_name AS contactName,
                   t.contact_phone AS contactPhone,
                   t.expire_at AS expireAt,
                   t.created_at AS createdAt,
                   t.updated_at AS updatedAt
            FROM sys_tenant t
            INNER JOIN sys_user u
                    ON u.tenant_id = t.tenant_id
                   AND u.username = #{username}
                   AND u.status = 'enabled'
            WHERE t.status = 'enabled'
            ORDER BY t.tenant_id
            """;
    }

    /**
     * 生成按租户业务编码查询单条租户 SQL。
     *
     * @return 租户查询 SQL
     */
    public String selectByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   tenant_name AS name,
                   package_id AS packageId,
                   isolation_mode AS isolationMode,
                   status,
                   contact_name AS contactName,
                   contact_phone AS contactPhone,
                   expire_at AS expireAt,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_tenant
            WHERE tenant_id = #{tenantId}
            """;
    }

    /**
     * 生成租户新增或更新 SQL。
     *
     * @return 租户保存 SQL
     */
    public String upsertTenant() {
        return """
            INSERT INTO sys_tenant (
                tenant_id,
                tenant_name,
                package_id,
                isolation_mode,
                status,
                contact_name,
                contact_phone,
                expire_at
            ) VALUES (
                #{tenantId},
                #{name},
                #{packageId},
                #{isolationMode},
                #{status},
                #{contactName},
                #{contactPhone},
                #{expireAt}
            )
            ON DUPLICATE KEY UPDATE
                tenant_name = VALUES(tenant_name),
                package_id = VALUES(package_id),
                isolation_mode = VALUES(isolation_mode),
                status = VALUES(status),
                contact_name = VALUES(contact_name),
                contact_phone = VALUES(contact_phone),
                expire_at = VALUES(expire_at),
                updated_at = CURRENT_TIMESTAMP
            """;
    }

    /**
     * 生成租户基础信息更新 SQL。
     *
     * @return 租户更新 SQL
     */
    public String updateTenant() {
        return """
            UPDATE sys_tenant
            SET tenant_name = #{name},
                package_id = #{packageId},
                isolation_mode = #{isolationMode},
                status = #{status},
                contact_name = #{contactName},
                contact_phone = #{contactPhone},
                expire_at = #{expireAt},
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId}
            """;
    }

    /**
     * 生成租户状态更新 SQL。
     *
     * @return 租户状态更新 SQL
     */
    public String updateStatus() {
        return """
            UPDATE sys_tenant
            SET status = #{status},
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId}
            """;
    }

    /**
     * 生成租户主记录删除 SQL。
     *
     * @return 租户主记录删除 SQL
     */
    public String deleteByTenantId() {
        return """
            DELETE FROM sys_tenant
            WHERE tenant_id = #{tenantId}
            """;
    }
}
