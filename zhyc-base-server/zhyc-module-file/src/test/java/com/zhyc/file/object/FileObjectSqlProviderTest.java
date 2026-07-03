/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object;

import com.zhyc.file.object.mapper.FileObjectSqlProvider;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件对象 SQL Provider 测试。
 */
class FileObjectSqlProviderTest {

  /**
   * 验证文件对象分页 SQL 包含租户、逻辑删除、关键词和分页条件。
   */
  @Test
  void shouldGenerateTenantPageSql() {
    String sql = new FileObjectSqlProvider().selectPageByQuery(Map.of("keyword", "pdf"));

    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND deleted = 0"));
    assertTrue(sql.contains("AND original_name LIKE CONCAT('%', #{keyword}, '%')"));
    assertTrue(sql.contains("LIMIT #{pageSize} OFFSET #{offset}"));
  }

  /**
   * 验证文件对象写入 SQL 明确字段，不使用 SELECT 星号或隐式字段。
   */
  @Test
  void shouldGenerateExplicitInsertSql() {
    String sql = new FileObjectSqlProvider().insert();

    assertTrue(sql.contains("INSERT INTO file_object"));
    assertTrue(sql.contains("tenant_id, file_code, storage_code"));
  }
}
