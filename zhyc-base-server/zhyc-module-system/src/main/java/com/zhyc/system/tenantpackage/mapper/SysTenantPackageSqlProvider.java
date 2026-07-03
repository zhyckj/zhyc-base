/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.mapper;

/**
 * 系统租户套餐 SQL Provider。
 */
public class SysTenantPackageSqlProvider {

    /**
     * 生成按状态查询租户套餐列表 SQL。
     *
     * @return 租户套餐列表查询 SQL
     */
    public String selectByStatus() {
        return """
            SELECT id,
                   package_code AS packageCode,
                   package_name AS packageName,
                   status,
                   max_user_count AS maxUserCount,
                   max_storage_mb AS maxStorageMb,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_tenant_package
            WHERE status = #{status}
            ORDER BY package_code
            """;
    }

    /**
     * 生成按套餐编码查询租户套餐 SQL。
     *
     * @return 租户套餐查询 SQL
     */
    public String selectByCode() {
        return """
            SELECT id,
                   package_code AS packageCode,
                   package_name AS packageName,
                   status,
                   max_user_count AS maxUserCount,
                   max_storage_mb AS maxStorageMb,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_tenant_package
            WHERE package_code = #{packageCode}
            """;
    }

    /**
     * 生成租户套餐创建 SQL。
     *
     * @return 租户套餐创建 SQL
     */
    public String insertPackage() {
        return """
            INSERT INTO sys_tenant_package (
                package_code,
                package_name,
                status,
                max_user_count,
                max_storage_mb
            ) VALUES (
                #{packageCode},
                #{packageName},
                #{status},
                #{maxUserCount},
                #{maxStorageMb}
            )
            """;
    }

    /**
     * 生成租户套餐状态更新 SQL。
     *
     * @return 租户套餐状态更新 SQL
     */
    public String updateStatus() {
        return """
            UPDATE sys_tenant_package
            SET status = #{status},
                updated_at = CURRENT_TIMESTAMP
            WHERE package_code = #{packageCode}
            """;
    }
}
