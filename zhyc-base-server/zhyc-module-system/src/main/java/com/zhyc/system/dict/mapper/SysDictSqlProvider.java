/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.mapper;

/**
 * 系统字典 SQL Provider。
 */
public class SysDictSqlProvider {

    /**
     * 生成租户字典类型列表查询 SQL。
     *
     * @return 字典类型列表查询 SQL
     */
    public String selectTypesByTenantId() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   dict_code AS dictCode,
                   dict_name AS dictName,
                   system_flag AS systemFlag,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_dict_type
            WHERE tenant_id = #{tenantId}
            ORDER BY dict_code
            """;
    }

    public String insertType() {
        return """
            INSERT INTO sys_dict_type (tenant_id, dict_code, dict_name, system_flag, status)
            VALUES (#{tenantId}, #{dictCode}, #{dictName}, #{systemFlag}, #{status})
            """;
    }

    public String updateType() {
        return """
            UPDATE sys_dict_type
            SET dict_code = #{dictCode},
                dict_name = #{dictName},
                system_flag = #{systemFlag},
                status = #{status}
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
            """;
    }

    public String deleteTypeByTenantIdAndId() {
        return """
            DELETE FROM sys_dict_type
            WHERE tenant_id = #{tenantId}
              AND id = #{typeId}
            """;
    }

    /**
     * 生成租户字典项列表查询 SQL。
     *
     * @return 字典项列表查询 SQL
     */
    public String selectItemsByTenantIdAndDictCode() {
        return """
            SELECT id,
                   tenant_id AS tenantId,
                   dict_code AS dictCode,
                   item_label AS itemLabel,
                   item_value AS itemValue,
                   item_color AS itemColor,
                   sort_order AS sortOrder,
                   status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM sys_dict_item
            WHERE tenant_id = #{tenantId}
              AND dict_code = #{dictCode}
            ORDER BY sort_order, id
            """;
    }

    public String insertItem() {
        return """
            INSERT INTO sys_dict_item (
                tenant_id, dict_code, item_label, item_value, item_color, sort_order, status
            ) VALUES (
                #{tenantId}, #{dictCode}, #{itemLabel}, #{itemValue}, #{itemColor}, #{sortOrder}, #{status}
            )
            """;
    }

    public String updateItem() {
        return """
            UPDATE sys_dict_item
            SET dict_code = #{dictCode},
                item_label = #{itemLabel},
                item_value = #{itemValue},
                item_color = #{itemColor},
                sort_order = #{sortOrder},
                status = #{status}
            WHERE tenant_id = #{tenantId}
              AND id = #{id}
            """;
    }

    public String deleteItemByTenantIdAndId() {
        return """
            DELETE FROM sys_dict_item
            WHERE tenant_id = #{tenantId}
              AND id = #{itemId}
            """;
    }
}
