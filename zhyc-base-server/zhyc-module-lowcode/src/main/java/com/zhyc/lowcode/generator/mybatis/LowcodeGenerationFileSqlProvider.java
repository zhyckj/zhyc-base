/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator.mybatis;

/**
 * 低代码生成文件明细 MyBatis SQL Provider。
 *
 * <p>集中维护生成文件明细 SQL，所有查询必须携带租户编码。</p>
 */
public class LowcodeGenerationFileSqlProvider {

  /**
   * 构建新增生成文件明细 SQL。
   *
   * @return 新增生成文件明细的参数化 SQL
   */
  public String insert() {
    return """
        INSERT INTO lc_generation_file
        (tenant_id, record_id, template_code, file_path, file_type, overwrite_mode, content_hash)
        VALUES
        (#{tenantId}, #{recordId}, #{templateCode}, #{filePath}, #{fileType}, #{overwriteMode}, #{contentHash})
        """.strip();
  }

  /**
   * 构建按租户和生成记录查询文件明细 SQL。
   *
   * @return 生成文件明细查询 SQL
   */
  public String selectByTenantIdAndRecordId() {
    return """
        SELECT id, tenant_id, record_id, template_code, file_path, file_type, overwrite_mode, content_hash
        FROM lc_generation_file
        WHERE tenant_id = #{tenantId}
          AND record_id = #{recordId}
        ORDER BY id ASC
        """.strip();
  }
}
