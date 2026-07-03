/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * MySQL 分页方言测试。
 */
class MySqlPaginationDialectTest {

  private final MySqlPaginationDialect dialect = new MySqlPaginationDialect();

  /**
   * 验证 MySQL 分页语句使用 LIMIT offset,pageSize 形式。
   */
  @Test
  void appliesLimitPagination() {
    String sql = dialect.applyPagination("SELECT * FROM sys_user", 20, 10);

    assertEquals("SELECT * FROM sys_user LIMIT 20, 10", sql);
  }

  /**
   * 验证非法分页参数会被拒绝。
   */
  @Test
  void rejectsNegativeOffset() {
    assertThrows(IllegalArgumentException.class,
        () -> dialect.applyPagination("SELECT 1", -1, 10));
  }
}
