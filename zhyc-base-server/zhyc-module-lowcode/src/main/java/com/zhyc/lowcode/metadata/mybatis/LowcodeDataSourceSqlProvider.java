/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

/**
 * 低代码数据源 MyBatis SQL Provider。
 *
 * <p>集中维护数据源元数据 SQL，保证租户隔离条件和敏感密钥引用字段一致。</p>
 */
public class LowcodeDataSourceSqlProvider {

  /**
   * 构建新增数据源 SQL。
   *
   * @return 新增数据源的参数化 SQL
   */
  public String insert() {
    return """
        INSERT INTO lowcode_data_source
        (tenant_id, code, name, dialect, jdbc_url, username, password_secret_ref, enabled)
        VALUES
        (#{tenantId}, #{code}, #{name}, #{dialect}, #{jdbcUrl}, #{username}, #{passwordSecretRef}, #{enabled})
        """.strip();
  }

  /**
   * 构建按租户和数据源编码查询 SQL。
   *
   * @return 带租户隔离条件的数据源查询 SQL
   */
  public String selectByTenantIdAndCode() {
    return """
        SELECT id, tenant_id, code, name, dialect, jdbc_url, username, password_secret_ref, enabled
        FROM lowcode_data_source
        WHERE tenant_id = #{tenantId} AND code = #{code}
        """.strip();
  }

  /**
   * 构建按租户和数据源主键查询 SQL。
   *
   * @return 带租户隔离条件的数据源主键查询 SQL
   */
  public String selectByTenantIdAndId() {
    return """
        SELECT id, tenant_id, code, name, dialect, jdbc_url, username, password_secret_ref, enabled
        FROM lowcode_data_source
        WHERE tenant_id = #{tenantId} AND id = #{id}
        """.strip();
  }

  /**
   * 构建按租户查询数据源列表 SQL。
   *
   * @return 带租户隔离条件的数据源列表查询 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT id, tenant_id, code, name, dialect, jdbc_url, username, password_secret_ref, enabled
        FROM lowcode_data_source
        WHERE tenant_id = #{tenantId}
        ORDER BY code ASC
        """.strip();
  }

  /**
   * 构建更新数据源 SQL。
   *
   * @return 通过租户和编码定位的数据源更新 SQL
   */
  public String updateByTenantIdAndCode() {
    return """
        UPDATE lowcode_data_source
        SET name = #{name},
            dialect = #{dialect},
            jdbc_url = #{jdbcUrl},
            username = #{username},
            password_secret_ref = #{passwordSecretRef},
            enabled = #{enabled},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId} AND code = #{code}
        """.strip();
  }
}
