/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db.mysql;

import com.zhyc.lowcode.db.FieldTypeMapper;
import com.zhyc.lowcode.db.FieldTypeMappingSupport;
import com.zhyc.lowcode.db.LowcodeColumn;
import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;

/**
 * MySQL 字段类型映射器。
 */
public class MySqlFieldTypeMapper implements FieldTypeMapper {

  /**
   * 返回 MySQL 方言名称。
   *
   * @return mysql
   */
  @Override
  public String getDialectName() {
    return LowcodeDatabaseDialect.MYSQL.getCode();
  }

  /**
   * 将平台统一字段类型映射为 MySQL 字段类型。
   *
   * @param column 低代码字段模型
   * @return MySQL 字段类型声明
   */
  @Override
  public String toDatabaseType(LowcodeColumn column) {
    LowcodeFieldType fieldType = column.getFieldType();
    switch (fieldType) {
      case STRING:
        return "VARCHAR(" + FieldTypeMappingSupport.requiredLength(column) + ")";
      case TEXT:
        return "TEXT";
      case INTEGER:
        return "INT";
      case LONG:
        return "BIGINT";
      case DECIMAL:
        return "DECIMAL(" + FieldTypeMappingSupport.requiredLength(column) + ","
            + FieldTypeMappingSupport.requiredScale(column) + ")";
      case BOOLEAN:
        return "TINYINT";
      case DATE:
        return "DATE";
      case DATETIME:
        return "DATETIME";
      default:
        throw new IllegalArgumentException("不支持的字段类型：" + fieldType);
    }
  }

}
