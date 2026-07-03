/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview;

import com.zhyc.file.preview.mapper.FilePreviewSqlProvider;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件预览 SQL 生成测试。
 */
class FilePreviewSqlProviderTest {

  /**
   * 验证预览日志写入 SQL 使用预览日志表。
   */
  @Test
  void shouldBuildInsertPreviewLogSql() {
    String sql = new FilePreviewSqlProvider().insertLog();

    assertTrue(sql.contains("INSERT INTO file_preview_log"));
    assertTrue(sql.contains("tenant_id, file_code, preview_type"));
  }

  /**
   * 验证预览日志查询 SQL 包含租户和文件编码过滤。
   */
  @Test
  void shouldBuildPreviewLogQueryWithTenantAndFileCode() {
    String sql = new FilePreviewSqlProvider().selectLogs(Map.of("tenantId", "tenant_a",
        "fileCode", "FILE001"));

    assertTrue(sql.contains("FROM file_preview_log"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("file_code = #{fileCode}"));
  }
}
