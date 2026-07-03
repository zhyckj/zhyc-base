/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.mapper;

import java.util.Map;

/**
 * 文件对象 SQL Provider。
 */
public class FileObjectSqlProvider {

  /**
   * 生成文件对象数量统计 SQL。
   *
   * @param params 查询参数
   * @return 文件对象数量统计 SQL
   */
  public String countByQuery(Map<String, Object> params) {
    return baseQuerySql("SELECT COUNT(1)", params, false);
  }

  /**
   * 生成文件对象分页查询 SQL。
   *
   * @param params 查询参数
   * @return 文件对象分页查询 SQL
   */
  public String selectPageByQuery(Map<String, Object> params) {
    return baseQuerySql("""
        SELECT id,
               tenant_id AS tenantId,
               file_code AS fileCode,
               storage_code AS storageCode,
               original_name AS originalName,
               content_type AS contentType,
               file_size AS fileSize,
               object_key AS objectKey,
               file_status AS fileStatus,
               uploader_id AS uploaderId,
               created_at AS createdAt
        """, params, true);
  }

  /**
   * 生成文件对象写入 SQL。
   *
   * @return 文件对象写入 SQL
   */
  public String insert() {
    return """
        INSERT INTO file_object (
            tenant_id, file_code, storage_code, original_name, content_type, file_size,
            object_key, file_status, uploader_id
        ) VALUES (
            #{tenantId}, #{fileCode}, #{storageCode}, #{originalName}, #{contentType}, #{fileSize},
            #{objectKey}, #{fileStatus}, #{uploaderId}
        )
        """;
  }

  private String baseQuerySql(String selectSql, Map<String, Object> params, boolean paging) {
    StringBuilder sql = new StringBuilder(selectSql)
        .append("""

            FROM file_object
            WHERE tenant_id = #{tenantId}
              AND deleted = 0
            """);
    Object keyword = params.get("keyword");
    if (keyword != null && !keyword.toString().isBlank()) {
      sql.append("  AND original_name LIKE CONCAT('%', #{keyword}, '%')\n");
    }
    if (paging) {
      sql.append("""
          ORDER BY created_at DESC, id DESC
          LIMIT #{pageSize} OFFSET #{offset}
          """);
    }
    return sql.toString();
  }
}
