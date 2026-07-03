/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

/**
 * 分页方言扩展接口。
 */
public interface PaginationDialect {

  /**
   * 返回当前分页方言名称。
   *
   * @return 数据库方言名称
   */
  String getDialectName();

  /**
   * 为查询 SQL 追加分页语法。
   *
   * @param sql 原始查询 SQL
   * @param offset 起始偏移量，从 0 开始
   * @param pageSize 每页返回数量
   * @return 追加分页后的查询 SQL
   */
  String applyPagination(String sql, long offset, long pageSize);
}
