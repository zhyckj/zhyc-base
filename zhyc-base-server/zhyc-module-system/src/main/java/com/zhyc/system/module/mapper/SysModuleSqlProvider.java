/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module.mapper;

/**
 * 系统模块 SQL Provider。
 */
public class SysModuleSqlProvider {

    /**
     * 生成系统模块清单查询 SQL。
     *
     * @return 系统模块清单查询 SQL
     */
    public String selectAllModules() {
        return """
            SELECT id,
                   module_code AS moduleCode,
                   module_name AS moduleName,
                   version,
                   module_type AS moduleType,
                   enabled,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_module
            ORDER BY module_code
            """;
    }

    /**
     * 生成模块依赖查询 SQL。
     *
     * @return 模块依赖查询 SQL
     */
    public String selectDependenciesByModuleCode() {
        return """
            SELECT id,
                   module_code AS moduleCode,
                   depends_on_code AS dependsOnCode,
                   required_version AS requiredVersion,
                   created_at AS createdAt
            FROM sys_module_dependency
            WHERE module_code = #{moduleCode}
            ORDER BY depends_on_code
            """;
    }

    /**
     * 生成模块资源查询 SQL。
     *
     * @return 模块资源查询 SQL
     */
    public String selectResourcesByModuleCode() {
        return """
            SELECT id,
                   module_code AS moduleCode,
                   resource_type AS resourceType,
                   resource_code AS resourceCode,
                   resource_path AS resourcePath,
                   created_at AS createdAt
            FROM sys_module_resource
            WHERE module_code = #{moduleCode}
            ORDER BY resource_type, resource_code
            """;
    }

    /**
     * 生成系统模块插入 SQL。
     *
     * @return 系统模块插入 SQL
     */
    public String insertModule() {
        return """
            INSERT INTO sys_module (
                module_code,
                module_name,
                version,
                module_type,
                enabled,
                created_at,
                updated_at
            ) VALUES (
                #{moduleCode},
                #{moduleName},
                #{version},
                #{moduleType},
                #{enabled},
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            )
            """;
    }

    /**
     * 生成系统模块基础信息更新 SQL。
     *
     * @return 系统模块基础信息更新 SQL
     */
    public String updateModule() {
        return """
            UPDATE sys_module
            SET module_name = #{moduleName},
                version = #{version},
                module_type = #{moduleType},
                enabled = #{enabled},
                updated_at = CURRENT_TIMESTAMP
            WHERE module_code = #{moduleCode}
            """;
    }

    /**
     * 生成指定模块依赖删除 SQL。
     *
     * @return 指定模块依赖删除 SQL
     */
    public String deleteDependenciesByModuleCode() {
        return """
            DELETE FROM sys_module_dependency
            WHERE module_code = #{moduleCode}
            """;
    }

    /**
     * 生成系统模块依赖插入 SQL。
     *
     * @return 系统模块依赖插入 SQL
     */
    public String insertDependency() {
        return """
            INSERT INTO sys_module_dependency (
                module_code,
                depends_on_code,
                required_version,
                created_at
            ) VALUES (
                #{moduleCode},
                #{dependsOnCode},
                #{requiredVersion},
                CURRENT_TIMESTAMP
            )
            """;
    }

    /**
     * 生成指定模块资源删除 SQL。
     *
     * @return 指定模块资源删除 SQL
     */
    public String deleteResourcesByModuleCode() {
        return """
            DELETE FROM sys_module_resource
            WHERE module_code = #{moduleCode}
            """;
    }

    /**
     * 生成系统模块资源插入 SQL。
     *
     * @return 系统模块资源插入 SQL
     */
    public String insertResource() {
        return """
            INSERT INTO sys_module_resource (
                module_code,
                resource_type,
                resource_code,
                resource_path,
                created_at
            ) VALUES (
                #{moduleCode},
                #{resourceType},
                #{resourceCode},
                #{resourcePath},
                CURRENT_TIMESTAMP
            )
            """;
    }

    /**
     * 生成模块启停状态更新 SQL。
     *
     * @return 模块启停状态更新 SQL
     */
    public String updateEnabled() {
        return """
            UPDATE sys_module
            SET enabled = #{enabled},
                updated_at = CURRENT_TIMESTAMP
            WHERE module_code = #{moduleCode}
            """;
    }
}
