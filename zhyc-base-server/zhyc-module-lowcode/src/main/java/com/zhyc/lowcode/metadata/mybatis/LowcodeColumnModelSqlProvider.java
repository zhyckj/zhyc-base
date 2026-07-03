/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

/**
 * 低代码字段模型 MyBatis SQL Provider。
 *
 * <p>字段模型按表模型重建，删除和查询必须通过表模型表补充租户边界。</p>
 */
public class LowcodeColumnModelSqlProvider {

  /**
   * 构建按租户和表模型删除字段 SQL。
   *
   * @return 删除指定租户表模型下字段的 SQL
   */
  public String deleteByTenantIdAndTableModelId() {
    return """
        DELETE c FROM lowcode_column_model c
        INNER JOIN lowcode_table_model t ON c.table_model_id = t.id
        WHERE t.tenant_id = #{tenantId} AND c.table_model_id = #{tableModelId}
        """.strip();
  }

  /**
   * 构建新增字段模型 SQL。
   *
   * @return 新增字段模型的参数化 SQL
   */
  public String insert() {
    return """
        INSERT INTO lowcode_column_model
        (table_model_id, code, name, field_type, length_value, scale_value,
         required, primary_key_flag, auto_increment_flag,
         list_visible, form_visible, queryable, dict_code, sort_order, comment)
        VALUES
        (#{tableModelId}, #{fieldCode}, #{fieldName}, #{fieldType}, #{lengthValue}, #{scaleValue},
         #{required}, #{primaryKey}, #{autoIncrement}, #{listVisible}, #{formVisible},
         #{queryable}, #{dictCode}, #{sortOrder}, #{comment})
        """.strip();
  }

  /**
   * 构建按租户和表模型查询字段 SQL。
   *
   * @return 按字段排序返回指定租户表模型字段的 SQL
   */
  public String selectByTenantIdAndTableModelId() {
    return """
        SELECT c.id, c.table_model_id, c.code AS field_code, c.name AS field_name, c.field_type,
               c.length_value, c.scale_value, c.required, c.primary_key_flag, c.auto_increment_flag,
               c.list_visible, c.form_visible, c.queryable, c.dict_code, c.sort_order, c.comment
        FROM lowcode_column_model c
        INNER JOIN lowcode_table_model t ON c.table_model_id = t.id
        WHERE t.tenant_id = #{tenantId} AND c.table_model_id = #{tableModelId}
        ORDER BY c.sort_order ASC, c.id ASC
        """.strip();
  }
}
