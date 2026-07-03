/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * 低代码生成目标枚举测试。
 */
class GenerationTargetTest {

  /**
   * 验证生成目标暴露稳定接口编码，避免前端和开放 API 依赖 Java 枚举名。
   */
  @Test
  void shouldExposeStableTargetCodes() {
    assertEquals("admin-backend", GenerationTarget.ADMIN_BACKEND.getCode());
    assertEquals("admin-frontend", GenerationTarget.ADMIN_FRONTEND.getCode());
    assertEquals("uniapp", GenerationTarget.UNIAPP.getCode());
    assertEquals("open-api-portal", GenerationTarget.OPEN_API_PORTAL.getCode());
    assertEquals("microservice-module", GenerationTarget.MICROSERVICE_MODULE.getCode());
  }

  /**
   * 验证生成目标可从稳定编码解析，并兼容既有 Java 枚举名。
   */
  @Test
  void shouldParseTargetFromCodeAndLegacyEnumName() {
    assertEquals(GenerationTarget.ADMIN_BACKEND, GenerationTarget.fromCode(" admin-backend "));
    assertEquals(GenerationTarget.OPEN_API_PORTAL, GenerationTarget.fromCode("OPEN_API_PORTAL"));

    assertThrows(IllegalArgumentException.class, () -> GenerationTarget.fromCode("unknown-target"));
  }
}
