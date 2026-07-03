/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

/**
 * 低代码表关系 MyBatis SQL Provider。
 *
 * <p>表关系除自身租户字段外，还必须校验主表和子表模型均归属当前租户。</p>
 */
public class LowcodeTableRelationSqlProvider {

  /**
   * 构建新增表关系 SQL。
   *
   * @return 新增表关系 SQL
   */
  public String insert() {
    return """
        INSERT INTO lowcode_table_relation
        (tenant_id, main_table_id, sub_table_id, relation_type, join_column, ref_column)
        VALUES
        (#{tenantId}, #{mainTableId}, #{subTableId}, #{relationType}, #{joinColumn}, #{refColumn})
        """.strip();
  }

  /**
   * 构建按租户查询表关系 SQL。
   *
   * @return 仅返回当前租户主表和子表关系的 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT r.id, r.tenant_id, r.main_table_id, r.sub_table_id, r.relation_type, r.join_column, r.ref_column
        FROM lowcode_table_relation r
        INNER JOIN lowcode_table_model main_table
          ON r.main_table_id = main_table.id AND main_table.tenant_id = #{tenantId}
        INNER JOIN lowcode_table_model sub_table
          ON r.sub_table_id = sub_table.id AND sub_table.tenant_id = #{tenantId}
        WHERE r.tenant_id = #{tenantId}
        ORDER BY r.id DESC
        """.strip();
  }

  /**
   * 构建按租户和表关系唯一键更新 SQL。
   *
   * @return 同时校验主子表租户归属的表关系更新 SQL
   */
  public String updateByTenantAndTables() {
    return """
        UPDATE lowcode_table_relation
        SET join_column = #{joinColumn},
            ref_column = #{refColumn},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId}
          AND main_table_id = #{mainTableId}
          AND sub_table_id = #{subTableId}
          AND relation_type = #{relationType}
          AND EXISTS (
              SELECT 1 FROM lowcode_table_model main_table
              WHERE main_table.id = #{mainTableId} AND main_table.tenant_id = #{tenantId}
          )
          AND EXISTS (
              SELECT 1 FROM lowcode_table_model sub_table
              WHERE sub_table.id = #{subTableId} AND sub_table.tenant_id = #{tenantId}
          )
        """.strip();
  }
}
