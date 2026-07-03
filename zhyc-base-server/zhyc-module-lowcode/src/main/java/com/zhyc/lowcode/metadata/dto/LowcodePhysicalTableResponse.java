/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodePhysicalTable;

/**
 * 数据源物理表响应。
 */
public class LowcodePhysicalTableResponse {

  /** 物理表名。 */
  private String tableName;
  /** 表注释。 */
  private String comment;
  /** 字段数量。 */
  private int columnCount;

  /**
   * 从物理表结构创建响应。
   *
   * @param physicalTable 物理表结构
   * @return 物理表响应
   */
  public static LowcodePhysicalTableResponse from(LowcodePhysicalTable physicalTable) {
    LowcodePhysicalTableResponse response = new LowcodePhysicalTableResponse();
    response.tableName = physicalTable.getTableName();
    response.comment = physicalTable.getComment();
    response.columnCount = physicalTable.getColumns().size();
    return response;
  }

  /**
   * 返回物理表名。
   *
   * @return 物理表名
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * 返回表注释。
   *
   * @return 表注释，未读取到时返回 {@code null}
   */
  public String getComment() {
    return comment;
  }

  /**
   * 返回字段数量。
   *
   * @return 字段数量
   */
  public int getColumnCount() {
    return columnCount;
  }
}
