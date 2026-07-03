/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 低代码生成记录响应测试。
 */
class LowcodeGenerationRecordResponseTest {

  /**
   * 验证生成记录响应暴露稳定目标端编码，避免前端依赖 Java 枚举名。
   */
  @Test
  void shouldExposeStableTargetCode() {
    LowcodeGenerationRecord record = LowcodeGenerationRecord.success(
        "tenant_a",
        "purchase_order",
        GenerationTarget.OPEN_API_PORTAL,
        "purchase",
        "purchaseOrder",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS,
        1);

    LowcodeGenerationRecordResponse response = LowcodeGenerationRecordResponse.from(record);

    assertEquals("open-api-portal", response.getTarget());
  }
}
