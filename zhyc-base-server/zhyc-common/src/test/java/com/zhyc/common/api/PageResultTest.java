/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 分页响应测试。
 */
class PageResultTest {

  /**
   * 验证分页响应会复制记录列表，避免外部修改影响接口响应。
   */
  @Test
  void shouldCopyRecordsAndExposePageFields() {
    List<String> sourceRecords = new ArrayList<>();
    sourceRecords.add("FILE001");

    PageResult<String> page = PageResult.of(12L, 2, 20, sourceRecords);
    sourceRecords.add("FILE002");

    assertEquals(12L, page.getTotal());
    assertEquals(2, page.getPageNo());
    assertEquals(20, page.getPageSize());
    assertEquals(List.of("FILE001"), page.getRecords());
    assertThrows(UnsupportedOperationException.class, () -> page.getRecords().add("FILE003"));
  }

  /**
   * 验证分页响应会归一非法页码和页大小，避免接口返回不可预期分页边界。
   */
  @Test
  void shouldNormalizeInvalidPageArguments() {
    PageResult<String> page = PageResult.of(-1L, 0, 200, null);

    assertEquals(0L, page.getTotal());
    assertEquals(1, page.getPageNo());
    assertEquals(100, page.getPageSize());
    assertEquals(List.of(), page.getRecords());
  }
}
