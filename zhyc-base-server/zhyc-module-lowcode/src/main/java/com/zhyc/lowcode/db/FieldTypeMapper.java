/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

/**
 * 字段类型映射扩展接口。
 */
public interface FieldTypeMapper {

  /**
   * 返回当前映射器支持的数据库方言名称。
   *
   * @return 数据库方言名称
   */
  String getDialectName();

  /**
   * 将平台统一字段类型映射为数据库字段类型。
   *
   * @param column 低代码字段模型
   * @return 数据库字段类型声明
   */
  String toDatabaseType(LowcodeColumn column);
}
