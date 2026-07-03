/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db.sqlserver;

import com.zhyc.lowcode.db.PaginationDialect;
import com.zhyc.lowcode.db.PaginationSupport;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;
import java.util.Locale;

/**
 * SQL Server 分页方言。
 */
public class SqlServerPaginationDialect implements PaginationDialect {

  /**
   * 返回 SQL Server 方言名称。
   *
   * @return sqlserver
   */
  @Override
  public String getDialectName() {
    return LowcodeDatabaseDialect.SQLSERVER.getCode();
  }

  /**
   * 为查询 SQL 追加 SQL Server OFFSET/FETCH 分页语法。
   *
   * <p>SQL Server 分页要求存在 ORDER BY；当上游 SQL 未声明排序时追加稳定兜底排序，避免运行期语法错误。</p>
   *
   * @param sql 原始查询 SQL
   * @param offset 起始偏移量，从 0 开始
   * @param pageSize 每页返回数量
   * @return 追加分页后的查询 SQL
   */
  @Override
  public String applyPagination(String sql, long offset, long pageSize) {
    PaginationSupport.validate(sql, offset, pageSize);
    String normalizedSql = sql.stripTrailing();
    if (!normalizedSql.toLowerCase(Locale.ROOT).contains(" order by ")) {
      normalizedSql = normalizedSql + " ORDER BY (SELECT NULL)";
    }
    return normalizedSql + " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
  }
}
