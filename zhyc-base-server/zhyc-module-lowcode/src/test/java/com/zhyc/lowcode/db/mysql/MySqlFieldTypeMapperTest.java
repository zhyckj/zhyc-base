/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.lowcode.db.LowcodeColumn;
import com.zhyc.lowcode.db.LowcodeFieldType;
import org.junit.jupiter.api.Test;

/**
 * MySQL 字段类型映射测试。
 */
class MySqlFieldTypeMapperTest {

  private final MySqlFieldTypeMapper mapper = new MySqlFieldTypeMapper();

  /**
   * 验证字符串字段会按配置长度映射为 VARCHAR。
   */
  @Test
  void mapsStringWithConfiguredLength() {
    LowcodeColumn column = LowcodeColumn.builder("username", LowcodeFieldType.STRING)
        .length(64)
        .build();

    assertEquals("VARCHAR(64)", mapper.toDatabaseType(column));
  }

  /**
   * 验证金额等小数字段会按精度和小数位映射为 DECIMAL。
   */
  @Test
  void mapsDecimalWithPrecisionAndScale() {
    LowcodeColumn column = LowcodeColumn.builder("amount", LowcodeFieldType.DECIMAL)
        .length(18)
        .scale(2)
        .build();

    assertEquals("DECIMAL(18,2)", mapper.toDatabaseType(column));
  }

  /**
   * 验证缺失字段长度时给出明确异常，避免生成不可控 DDL。
   */
  @Test
  void rejectsStringWithoutLength() {
    LowcodeColumn column = LowcodeColumn.builder("name", LowcodeFieldType.STRING).build();

    assertThrows(IllegalArgumentException.class, () -> mapper.toDatabaseType(column));
  }
}
