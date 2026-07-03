/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import java.util.ArrayList;
import java.util.List;

/**
 * 低代码数据表模型。
 */
public class LowcodeTable {

  /** 数据表名称。 */
  private final String name;
  /** 数据表注释。 */
  private final String comment;
  /** 数据表字段列表。 */
  private final List<LowcodeColumn> columns;

  /**
   * 创建低代码数据表模型。
   *
   * @param name 数据表名称
   * @param comment 数据表注释
   * @param columns 数据表字段列表
   */
  public LowcodeTable(String name, String comment, List<LowcodeColumn> columns) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("数据表名称不能为空");
    }
    if (columns == null || columns.isEmpty()) {
      throw new IllegalArgumentException("数据表字段不能为空");
    }
    this.name = name.trim();
    this.comment = comment;
    this.columns = new ArrayList<>(columns);
  }

  /**
   * 返回数据表名称。
   *
   * @return 数据表名称
   */
  public String getName() {
    return name;
  }

  /**
   * 返回数据表注释。
   *
   * @return 数据表注释，未配置时返回 {@code null}
   */
  public String getComment() {
    return comment;
  }

  /**
   * 返回数据表字段列表副本。
   *
   * @return 数据表字段列表副本
   */
  public List<LowcodeColumn> getColumns() {
    return new ArrayList<>(columns);
  }
}
