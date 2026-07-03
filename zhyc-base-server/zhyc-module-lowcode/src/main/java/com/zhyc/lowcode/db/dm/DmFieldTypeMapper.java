/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db.dm;

import com.zhyc.lowcode.db.FieldTypeMapper;
import com.zhyc.lowcode.db.FieldTypeMappingSupport;
import com.zhyc.lowcode.db.LowcodeColumn;
import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;

/**
 * 达梦数据库字段类型映射器。
 */
public class DmFieldTypeMapper implements FieldTypeMapper {

  /**
   * 返回达梦数据库方言名称。
   *
   * @return dm
   */
  @Override
  public String getDialectName() {
    return LowcodeDatabaseDialect.DM.getCode();
  }

  /**
   * 将平台统一字段类型映射为达梦数据库字段类型。
   *
   * @param column 低代码字段模型
   * @return 达梦数据库字段类型声明
   */
  @Override
  public String toDatabaseType(LowcodeColumn column) {
    LowcodeFieldType fieldType = column.getFieldType();
    return switch (fieldType) {
      case STRING -> "VARCHAR(" + FieldTypeMappingSupport.requiredLength(column) + ")";
      case TEXT -> "CLOB";
      case INTEGER -> "INT";
      case LONG -> "BIGINT";
      case DECIMAL -> "NUMBER(" + FieldTypeMappingSupport.requiredLength(column) + ","
          + FieldTypeMappingSupport.requiredScale(column) + ")";
      case BOOLEAN -> "NUMBER(1)";
      case DATE -> "DATE";
      case DATETIME -> "TIMESTAMP";
    };
  }
}
