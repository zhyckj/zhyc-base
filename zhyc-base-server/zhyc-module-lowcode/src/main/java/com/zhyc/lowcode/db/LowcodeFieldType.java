/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

/**
 * 低代码数据建模字段类型。
 *
 * <p>该枚举描述平台内部统一字段类型，不直接等同于具体数据库字段类型。</p>
 */
public enum LowcodeFieldType {

  /** 字符串字段，通常映射为 VARCHAR。 */
  STRING,
  /** 长文本字段，通常映射为 TEXT。 */
  TEXT,
  /** 32 位整数字段。 */
  INTEGER,
  /** 64 位整数字段。 */
  LONG,
  /** 定点小数字段。 */
  DECIMAL,
  /** 布尔字段。 */
  BOOLEAN,
  /** 日期字段，不包含时间。 */
  DATE,
  /** 日期时间字段。 */
  DATETIME
}
