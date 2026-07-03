/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db.dm;

import com.zhyc.lowcode.db.PaginationDialect;
import com.zhyc.lowcode.db.PaginationSupport;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;

/**
 * 达梦数据库分页方言。
 */
public class DmPaginationDialect implements PaginationDialect {

  /**
   * 返回达梦数据库方言名称。
   *
   * @return dm
   */
  @Override
  public String getDialectName() {
    return LowcodeDatabaseDialect.DM.getCode();
  }

  /**
   * 为查询 SQL 追加达梦 OFFSET/FETCH 分页语法。
   *
   * @param sql 原始查询 SQL
   * @param offset 起始偏移量，从 0 开始
   * @param pageSize 每页返回数量
   * @return 追加分页后的查询 SQL
   */
  @Override
  public String applyPagination(String sql, long offset, long pageSize) {
    PaginationSupport.validate(sql, offset, pageSize);
    return sql.stripTrailing() + " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
  }
}
