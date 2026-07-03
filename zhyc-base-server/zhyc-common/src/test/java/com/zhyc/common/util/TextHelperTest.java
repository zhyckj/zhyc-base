/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 文本帮助类测试。
 */
class TextHelperTest {

  @Test
  void requireTextShouldTrimValueAndRejectBlank() {
    assertEquals("tenant-a", TextHelper.requireText(" tenant-a ", "租户不能为空"));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> TextHelper.requireText(" ", "租户不能为空"));
    assertEquals("租户不能为空", exception.getMessage());
  }

  @Test
  void requireNoWhitespaceTextShouldKeepRawValueAndRejectAnyWhitespace() {
    assertEquals("secret:key", TextHelper.requireNoWhitespaceText("secret:key", "密钥不能为空", "密钥不能包含空白"));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> TextHelper.requireNoWhitespaceText(" secret:key", "密钥不能为空", "密钥不能包含空白"));
    assertEquals("密钥不能包含空白", exception.getMessage());
  }

  @Test
  void trimHelpersShouldNormalizeBlankValues() {
    assertNull(TextHelper.trimToNull("  "));
    assertEquals("", TextHelper.trimToEmpty(null));
    assertEquals("fallback", TextHelper.defaultIfBlank(" ", "fallback"));
  }

  @Test
  void shouldDetectWhitespaceAndRemoveTrailingRepeatedSuffix() {
    assertTrue(TextHelper.containsWhitespace("tenant a"));
    assertFalse(TextHelper.containsWhitespace("tenant-a"));
    assertEquals("zhyc", TextHelper.removeTrailingRepeated("zhyc::", ":"));
  }
}
