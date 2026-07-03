/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.mapper;

/**
 * 租户套餐模块授权 SQL Provider。
 */
public class SysTenantPackageModuleSqlProvider {

    /**
     * 生成套餐授权资源查询 SQL。
     *
     * @return 套餐授权资源查询 SQL
     */
    public String selectByPackageId() {
        return """
            SELECT id,
                   package_id AS packageId,
                   module_code AS moduleCode,
                   menu_code AS menuCode,
                   permission,
                   created_at AS createdAt
            FROM sys_tenant_package_module
            WHERE package_id = #{packageId}
            ORDER BY module_code, menu_code, permission
            """;
    }

    /**
     * 生成按套餐删除授权资源 SQL。
     *
     * @return 按套餐删除授权资源 SQL
     */
    public String deleteByPackageId() {
        return """
            DELETE FROM sys_tenant_package_module
            WHERE package_id = #{packageId}
            """;
    }

    /**
     * 生成新增套餐授权资源 SQL。
     *
     * @return 新增套餐授权资源 SQL
     */
    public String insert() {
        return """
            INSERT INTO sys_tenant_package_module (
                package_id,
                module_code,
                menu_code,
                permission
            ) VALUES (
                #{packageId},
                #{moduleCode},
                #{menuCode},
                #{permission}
            )
            """;
    }
}
