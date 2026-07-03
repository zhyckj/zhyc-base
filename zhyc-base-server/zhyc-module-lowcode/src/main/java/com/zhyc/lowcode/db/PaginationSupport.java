/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

/**
 * 分页方言参数校验工具。
 */
public final class PaginationSupport {

  private PaginationSupport() {
  }

  /**
   * 校验分页 SQL、偏移量和页大小。
   *
   * @param sql 原始查询 SQL
   * @param offset 起始偏移量，从 0 开始
   * @param pageSize 每页数量
   */
  public static void validate(String sql, long offset, long pageSize) {
    if (sql == null || sql.trim().isEmpty()) {
      throw new IllegalArgumentException("分页 SQL 不能为空");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("分页偏移量不能小于 0");
    }
    if (pageSize <= 0) {
      throw new IllegalArgumentException("分页大小必须大于 0");
    }
  }
}
