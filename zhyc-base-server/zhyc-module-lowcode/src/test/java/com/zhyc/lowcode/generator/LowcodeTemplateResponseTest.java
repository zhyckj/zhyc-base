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
 * 低代码生成模板响应测试。
 */
class LowcodeTemplateResponseTest {

  /**
   * 验证模板响应暴露稳定目标端编码，便于前端和开放 API 复用。
   */
  @Test
  void shouldExposeStableTargetCode() {
    CodeTemplateDescriptor template = new CodeTemplateDescriptor(
        "admin-backend-controller",
        GenerationTarget.ADMIN_BACKEND,
        "后台 Controller 模板",
        "src/main/java/{entityName}Controller.java");

    LowcodeTemplateResponse response = LowcodeTemplateResponse.from(template);

    assertEquals("admin-backend", response.getTarget());
  }
}
