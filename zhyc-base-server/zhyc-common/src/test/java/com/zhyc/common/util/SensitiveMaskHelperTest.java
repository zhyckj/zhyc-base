/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 敏感信息脱敏帮助类测试。
 */
class SensitiveMaskHelperTest {

  @Test
  void maskMiddleShouldKeepConfiguredPrefixAndSuffix() {
    assertEquals("abc****xyz", SensitiveMaskHelper.maskMiddle("abcdefxyz", 3, 3));
    assertEquals("****", SensitiveMaskHelper.maskMiddle("abc", 2, 2));
    assertEquals("", SensitiveMaskHelper.maskMiddle(null, 2, 2));
  }

  @Test
  void shouldMaskCommonSensitiveValues() {
    assertEquals("138****5678", SensitiveMaskHelper.maskMobile("13812345678"));
    assertEquals("a****@example.com", SensitiveMaskHelper.maskEmail("admin@example.com"));
    assertEquals("sk-****3456", SensitiveMaskHelper.maskSecret("sk-abcdef123456"));
  }
}
