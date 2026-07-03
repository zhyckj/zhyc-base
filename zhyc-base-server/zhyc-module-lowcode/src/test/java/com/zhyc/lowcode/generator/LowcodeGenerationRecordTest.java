/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 低代码生成记录领域模型测试。
 */
class LowcodeGenerationRecordTest {

  /**
   * 验证成功生成记录会保存租户、表模型、目标端、覆盖策略和文件数量。
   */
  @Test
  void shouldCreateSuccessRecord() {
    LowcodeGenerationRecord record = LowcodeGenerationRecord.success(
        "tenant_a",
        "purchase_order",
        GenerationTarget.ADMIN_BACKEND,
        "purchase",
        "purchaseOrder",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS,
        2,
        "[{\"targetPath\":\"src/a.java\"}]");

    assertEquals("tenant_a", record.getTenantId());
    assertEquals("purchase_order", record.getTableModelCode());
    assertEquals(GenerationTarget.ADMIN_BACKEND, record.getTarget());
    assertEquals("purchase", record.getModuleName());
    assertEquals("purchaseOrder", record.getEntityName());
    assertEquals(GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS, record.getOverwriteStrategy());
    assertEquals(2, record.getFileCount());
    assertEquals("[{\"targetPath\":\"src/a.java\"}]", record.getFileManifestJson());
    assertEquals(LowcodeGenerationRecordStatus.SUCCESS, record.getStatus());
    assertEquals("", record.getErrorMessage());
  }

  /**
   * 验证失败生成记录会保留失败原因，方便后台追踪生成问题。
   */
  @Test
  void shouldCreateFailedRecordWithErrorMessage() {
    LowcodeGenerationRecord record = LowcodeGenerationRecord.failed(
        "tenant_a",
        "purchase_order",
        GenerationTarget.ADMIN_FRONTEND,
        "purchase",
        "purchaseOrder",
        GeneratedFileOverwriteStrategy.SKIP_IF_EXISTS,
        1,
        "生成文件已存在");

    assertEquals(LowcodeGenerationRecordStatus.FAILED, record.getStatus());
    assertEquals("生成文件已存在", record.getErrorMessage());
  }

  /**
   * 验证生成文件数量不能为负数。
   */
  @Test
  void shouldRejectNegativeFileCount() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> LowcodeGenerationRecord.success(
            "tenant_a",
            "purchase_order",
            GenerationTarget.UNIAPP,
            "purchase",
            "purchaseOrder",
            GeneratedFileOverwriteStrategy.OVERWRITE,
            -1));

    assertEquals("生成文件数量不能为负数", exception.getMessage());
  }

  /**
   * 验证生成记录拒绝包含空白字符的模块名和实体名，避免非法命名写入生成历史。
   */
  @Test
  void shouldRejectRecordNamesContainingWhitespace() {
    IllegalArgumentException moduleException = assertThrows(IllegalArgumentException.class,
        () -> LowcodeGenerationRecord.success(
            "tenant_a",
            "purchase_order",
            GenerationTarget.ADMIN_BACKEND,
            "purchase order",
            "purchaseOrder",
            GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS,
            1));
    IllegalArgumentException entityException = assertThrows(IllegalArgumentException.class,
        () -> LowcodeGenerationRecord.failed(
            "tenant_a",
            "purchase_order",
            GenerationTarget.ADMIN_BACKEND,
            "purchase",
            "purchase Order",
            GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS,
            1,
            "生成失败"));

    assertEquals("业务模块名称不能包含空白字符", moduleException.getMessage());
    assertEquals("业务实体名称不能包含空白字符", entityException.getMessage());
  }
}
