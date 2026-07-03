/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * ID 帮助类测试。
 */
class IdHelperTest {

  @Test
  void shouldGenerateUuidAndUuidWithoutDash() {
    String uuid = IdHelper.uuid();
    String uuidNoDash = IdHelper.uuidNoDash();

    assertEquals(36, uuid.length());
    assertTrue(uuid.contains("-"));
    assertEquals(32, uuidNoDash.length());
    assertFalse(uuidNoDash.contains("-"));
  }

  @Test
  void randomBase62ShouldRespectLengthAndCharacterSet() {
    String value = IdHelper.randomBase62(24);

    assertEquals(24, value.length());
    assertTrue(value.matches("[0-9A-Za-z]+"));
    assertThrows(IllegalArgumentException.class, () -> IdHelper.randomBase62(0));
  }
}
