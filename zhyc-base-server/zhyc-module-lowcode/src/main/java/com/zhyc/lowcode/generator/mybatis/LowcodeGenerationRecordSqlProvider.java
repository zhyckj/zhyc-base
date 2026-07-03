/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator.mybatis;

/**
 * 低代码生成记录 MyBatis SQL Provider。
 *
 * <p>集中维护生成记录 SQL，确保所有查询按租户隔离。</p>
 */
public class LowcodeGenerationRecordSqlProvider {

  /**
   * 构建新增生成记录 SQL。
   *
   * @return 新增生成记录的参数化 SQL
   */
  public String insert() {
    return """
        INSERT INTO lowcode_generation_record
        (tenant_id, table_model_code, target, module_name, entity_name,
         overwrite_strategy, file_count, file_manifest_json, status, error_message)
        VALUES
        (#{tenantId}, #{tableModelCode}, #{target}, #{moduleName}, #{entityName},
         #{overwriteStrategy}, #{fileCount}, #{fileManifestJson}, #{status}, #{errorMessage})
        """.strip();
  }

  /**
   * 构建按租户查询生成记录列表 SQL。
   *
   * @return 带租户隔离条件的生成记录列表 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT id, tenant_id, table_model_code, target, module_name, entity_name,
               overwrite_strategy, file_count, file_manifest_json, status, error_message
        FROM lowcode_generation_record
        WHERE tenant_id = #{tenantId}
        ORDER BY id DESC
        """.strip();
  }
}
