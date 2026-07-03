/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

/**
 * 低代码表模型 MyBatis SQL Provider。
 *
 * <p>表模型属于租户内元数据，所有查询和更新必须带租户条件。</p>
 */
public class LowcodeTableModelSqlProvider {

  /**
   * 构建新增表模型 SQL。
   *
   * @return 新增表模型的参数化 SQL
   */
  public String insert() {
    return """
        INSERT INTO lowcode_table_model
        (tenant_id, data_source_id, code, name, table_name, status)
        VALUES
        (#{tenantId}, #{dataSourceId}, #{code}, #{name}, #{tableName}, #{status})
        """.strip();
  }

  /**
   * 构建按租户和模型编码查询 SQL。
   *
   * @return 带租户隔离条件的表模型查询 SQL
   */
  public String selectByTenantIdAndCode() {
    return """
        SELECT id, tenant_id, data_source_id, code, name, table_name, status
        FROM lowcode_table_model
        WHERE tenant_id = #{tenantId} AND code = #{code}
        """.strip();
  }

  /**
   * 构建按租户和主键查询表模型 SQL。
   *
   * @return 带租户隔离条件的表模型主键查询 SQL
   */
  public String selectByTenantIdAndId() {
    return """
        SELECT id, tenant_id, data_source_id, code, name, table_name, status
        FROM lowcode_table_model
        WHERE tenant_id = #{tenantId} AND id = #{id}
        """.strip();
  }

  /**
   * 构建按租户查询表模型列表 SQL。
   *
   * @return 带租户隔离条件的表模型列表查询 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT id, tenant_id, data_source_id, code, name, table_name, status
        FROM lowcode_table_model
        WHERE tenant_id = #{tenantId}
        ORDER BY code ASC
        """.strip();
  }

  /**
   * 构建更新表模型 SQL。
   *
   * @return 通过租户和编码定位的表模型更新 SQL
   */
  public String updateByTenantIdAndCode() {
    return """
        UPDATE lowcode_table_model
        SET data_source_id = #{dataSourceId},
            name = #{name},
            table_name = #{tableName},
            status = #{status},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId} AND code = #{code}
        """.strip();
  }

  /**
   * 构建发布状态更新 SQL。
   *
   * @return 通过租户和编码定位的状态更新 SQL
   */
  public String updateStatusByTenantIdAndCode() {
    return """
        UPDATE lowcode_table_model
        SET status = #{status},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId} AND code = #{code}
        """.strip();
  }
}
