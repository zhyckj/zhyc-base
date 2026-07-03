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
 * 低代码生成文件预览响应测试。
 */
class LowcodeGeneratedFileResponseTest {

  /**
   * 验证预览文件响应暴露稳定内容哈希，便于生成报告和覆盖风险比对。
   */
  @Test
  void shouldExposeStableContentHashForPreviewFile() {
    GeneratedFile file = new GeneratedFile(
        GenerationTarget.ADMIN_FRONTEND,
        "admin-frontend-list",
        "zhyc-base-vue/src/views/purchase/order/index.vue",
        "hello");

    LowcodeGeneratedFileResponse response = LowcodeGeneratedFileResponse.from(file);

    assertEquals("admin-frontend", response.getTarget());
    assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
        response.getContentHash());
  }
}
