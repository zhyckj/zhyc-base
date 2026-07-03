/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 集合帮助类测试。
 */
class CollectionHelperTest {

  @Test
  void shouldJudgeCollectionAndMapEmptySafely() {
    assertTrue(CollectionHelper.isEmpty((List<String>) null));
    assertTrue(CollectionHelper.isEmpty(List.of()));
    assertTrue(CollectionHelper.isEmpty((Map<String, String>) null));
    assertTrue(CollectionHelper.isEmpty(Map.of()));
  }

  @Test
  void nullToEmptyListShouldReturnImmutableList() {
    assertTrue(CollectionHelper.nullToEmptyList(null).isEmpty());
    List<String> values = CollectionHelper.nullToEmptyList(List.of("a", "b"));

    assertEquals(List.of("a", "b"), values);
    assertThrows(UnsupportedOperationException.class, () -> values.add("c"));
  }
}
