/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage.mapper;

/**
 * 文件存储配置 SQL Provider。
 */
public class FileStorageConfigSqlProvider {

  /**
   * 生成租户文件存储配置查询 SQL。
   *
   * @return 租户文件存储配置查询 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               storage_code AS storageCode,
               storage_name AS storageName,
               storage_type AS storageType,
               endpoint,
               status,
               default_flag AS defaultFlag,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM file_storage_config
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        ORDER BY default_flag DESC, storage_code
        """;
  }

  /**
   * 生成文件存储配置新增或更新 SQL。
   *
   * @return 文件存储配置新增或更新 SQL
   */
  public String upsert() {
    return """
        INSERT INTO file_storage_config (
            tenant_id, storage_code, storage_name, storage_type, endpoint, status, default_flag
        ) VALUES (
            #{tenantId}, #{storageCode}, #{storageName}, #{storageType}, #{endpoint}, #{status}, #{defaultFlag}
        )
        ON DUPLICATE KEY UPDATE
            storage_name = VALUES(storage_name),
            storage_type = VALUES(storage_type),
            endpoint = VALUES(endpoint),
            status = VALUES(status),
            default_flag = VALUES(default_flag),
            updated_at = CURRENT_TIMESTAMP,
            version = version + 1
        """;
  }
}
