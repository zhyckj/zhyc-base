/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db.sqlserver;

import com.zhyc.lowcode.db.FieldTypeMapper;
import com.zhyc.lowcode.db.FieldTypeMappingSupport;
import com.zhyc.lowcode.db.LowcodeColumn;
import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;

/**
 * SQL Server 字段类型映射器。
 */
public class SqlServerFieldTypeMapper implements FieldTypeMapper {

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
   * 将平台统一字段类型映射为 SQL Server 字段类型。
   *
   * @param column 低代码字段模型
   * @return SQL Server 字段类型声明
   */
  @Override
  public String toDatabaseType(LowcodeColumn column) {
    LowcodeFieldType fieldType = column.getFieldType();
    return switch (fieldType) {
      case STRING -> "NVARCHAR(" + FieldTypeMappingSupport.requiredLength(column) + ")";
      case TEXT -> "NVARCHAR(MAX)";
      case INTEGER -> "INT";
      case LONG -> "BIGINT";
      case DECIMAL -> "DECIMAL(" + FieldTypeMappingSupport.requiredLength(column) + ","
          + FieldTypeMappingSupport.requiredScale(column) + ")";
      case BOOLEAN -> "BIT";
      case DATE -> "DATE";
      case DATETIME -> "DATETIME2";
    };
  }
}
