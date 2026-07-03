/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage;

import com.zhyc.file.storage.mapper.FileStorageConfigSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件存储配置 SQL Provider 测试。
 */
class FileStorageConfigSqlProviderTest {

  /**
   * 验证文件存储配置查询 SQL 包含租户隔离和逻辑删除条件。
   */
  @Test
  void shouldGenerateTenantIsolatedQuerySql() {
    String sql = new FileStorageConfigSqlProvider().selectByTenantId();

    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND deleted = 0"));
  }

  /**
   * 验证文件存储配置保存 SQL 使用唯一键更新，不出现无条件更新。
   */
  @Test
  void shouldGenerateSafeUpsertSql() {
    String sql = new FileStorageConfigSqlProvider().upsert();

    assertTrue(sql.contains("INSERT INTO file_storage_config"));
    assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    assertTrue(sql.contains("version = version + 1"));
  }
}
