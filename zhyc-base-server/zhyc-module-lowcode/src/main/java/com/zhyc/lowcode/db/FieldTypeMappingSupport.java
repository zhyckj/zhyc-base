/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

/**
 * 数据库字段类型映射辅助工具。
 *
 * <p>集中处理长度、精度和小数位校验，保证不同数据库方言在字段约束缺失时返回一致错误。</p>
 */
public final class FieldTypeMappingSupport {

  private FieldTypeMappingSupport() {
  }

  /**
   * 读取必填长度或数值精度。
   *
   * @param column 低代码字段模型
   * @return 字段长度或数值精度
   */
  public static int requiredLength(LowcodeColumn column) {
    if (column.getLength() == null) {
      throw new IllegalArgumentException("字段 " + column.getName() + " 必须配置长度");
    }
    return column.getLength();
  }

  /**
   * 读取必填小数位数。
   *
   * @param column 低代码字段模型
   * @return 字段小数位数
   */
  public static int requiredScale(LowcodeColumn column) {
    if (column.getScale() == null) {
      throw new IllegalArgumentException("字段 " + column.getName() + " 必须配置小数位数");
    }
    return column.getScale();
  }
}
