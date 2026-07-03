/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.mapper;

import java.util.Map;

/**
 * 文件预览 SQL Provider。
 */
public class FilePreviewSqlProvider {

  /**
   * 生成文件预览日志写入 SQL。
   *
   * @return 文件预览日志写入 SQL
   */
  public String insertLog() {
    return """
        INSERT INTO file_preview_log (
            tenant_id, file_code, preview_type, preview_url, result, cost_ms
        ) VALUES (
            #{tenantId}, #{fileCode}, #{previewType}, #{previewUrl}, #{result}, #{costMs}
        )
        """;
  }

  /**
   * 生成文件预览日志查询 SQL。
   *
   * @param params 查询参数
   * @return 文件预览日志查询 SQL
   */
  public String selectLogs(Map<String, Object> params) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               file_code AS fileCode,
               preview_type AS previewType,
               preview_url AS previewUrl,
               result,
               cost_ms AS costMs,
               created_at AS createdAt
        FROM file_preview_log
        WHERE tenant_id = #{tenantId}
        """);
    Object fileCode = params.get("fileCode");
    if (fileCode != null && !fileCode.toString().isBlank()) {
      sql.append("  AND file_code = #{fileCode}\n");
    }
    sql.append("ORDER BY id DESC\n");
    return sql.toString();
  }
}
