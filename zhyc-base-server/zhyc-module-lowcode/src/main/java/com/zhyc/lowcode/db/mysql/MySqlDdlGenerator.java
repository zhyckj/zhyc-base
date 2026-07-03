/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db.mysql;

import com.zhyc.lowcode.db.DdlGenerator;
import com.zhyc.lowcode.db.DdlGenerationSupport;
import com.zhyc.lowcode.db.FieldTypeMapper;
import com.zhyc.lowcode.db.LowcodeColumn;
import com.zhyc.lowcode.db.LowcodeTable;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;
import java.util.List;
import java.util.Objects;

/**
 * MySQL DDL 生成器。
 */
public class MySqlDdlGenerator implements DdlGenerator {

  /** 字段类型映射器。 */
  private final FieldTypeMapper fieldTypeMapper;

  /**
   * 创建 MySQL DDL 生成器。
   *
   * @param fieldTypeMapper 字段类型映射器
   */
  public MySqlDdlGenerator(FieldTypeMapper fieldTypeMapper) {
    this.fieldTypeMapper = Objects.requireNonNull(fieldTypeMapper, "字段类型映射器不能为空");
  }

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
   * 生成 MySQL 创建数据表 DDL。
   *
   * @param table 低代码数据表模型
   * @return MySQL 创建数据表 DDL
   */
  @Override
  public String generateCreateTable(LowcodeTable table) {
    List<LowcodeColumn> columns = table.getColumns();
    StringBuilder ddl = new StringBuilder();
    ddl.append("CREATE TABLE IF NOT EXISTS ")
        .append(quote(table.getName()))
        .append(" (\n");
    for (int i = 0; i < columns.size(); i++) {
      ddl.append("    ").append(columnDefinition(columns.get(i))).append(",\n");
    }
    ddl.append("    PRIMARY KEY (")
        .append(DdlGenerationSupport.primaryKeys(columns, MySqlDdlGenerator::quote))
        .append(")");
    if (DdlGenerationSupport.needsTenantDeletedIndex(columns)) {
      ddl.append(",\n    KEY ")
          .append(quote(DdlGenerationSupport.tenantDeletedIndexName(table.getName())))
          .append(" (`tenant_id`, `deleted`)");
    }
    ddl.append("\n")
        .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    if (DdlGenerationSupport.hasText(table.getComment())) {
      ddl.append(" COMMENT='").append(DdlGenerationSupport.escapeComment(table.getComment())).append("'");
    }
    ddl.append(";");
    return ddl.toString();
  }

  private String columnDefinition(LowcodeColumn column) {
    StringBuilder definition = new StringBuilder();
    definition.append(quote(column.getName()))
        .append(" ")
        .append(fieldTypeMapper.toDatabaseType(column))
        .append(column.isNullable() ? " NULL" : " NOT NULL");
    if (column.isAutoIncrement()) {
      definition.append(" AUTO_INCREMENT");
    }
    String defaultClause = commonColumnDefaultClause(column);
    if (!defaultClause.isBlank()) {
      definition.append(" ").append(defaultClause);
    }
    if (DdlGenerationSupport.hasText(column.getComment())) {
      definition.append(" COMMENT '").append(DdlGenerationSupport.escapeComment(column.getComment())).append("'");
    }
    return definition.toString();
  }

  private static String commonColumnDefaultClause(LowcodeColumn column) {
    return switch (column.getName().toLowerCase()) {
      case "created_at" -> "DEFAULT CURRENT_TIMESTAMP";
      case "updated_at" -> "DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
      case "deleted", "version" -> "DEFAULT 0";
      default -> "";
    };
  }

  private static String quote(String identifier) {
    return "`" + DdlGenerationSupport.requireIdentifier(identifier) + "`";
  }
}
