/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据源物理表结构。
 *
 * <p>用于在线建模从已有数据库表反向生成低代码表模型，仅保存表名、注释和字段元数据。</p>
 */
public class LowcodePhysicalTable {

  /** 物理表名。 */
  private final String tableName;
  /** 表注释。 */
  private final String comment;
  /** 物理字段列表。 */
  private final List<LowcodePhysicalColumn> columns;

  /**
   * 创建物理表结构。
   *
   * @param tableName 物理表名
   * @param comment 表注释
   * @param columns 物理字段列表
   */
  public LowcodePhysicalTable(String tableName, String comment, List<LowcodePhysicalColumn> columns) {
    this.tableName = requireText(tableName, "物理表名不能为空");
    this.comment = trimToNull(comment);
    this.columns = columns == null ? List.of() : new ArrayList<>(columns);
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
   * 返回物理字段列表副本。
   *
   * @return 物理字段列表副本
   */
  public List<LowcodePhysicalColumn> getColumns() {
    return new ArrayList<>(columns);
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  private static String trimToNull(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return value.trim();
  }
}
