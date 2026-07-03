/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db.sqlserver;

import com.zhyc.lowcode.db.DdlGenerationSupport;
import com.zhyc.lowcode.db.DdlGenerator;
import com.zhyc.lowcode.db.FieldTypeMapper;
import com.zhyc.lowcode.db.LowcodeColumn;
import com.zhyc.lowcode.db.LowcodeTable;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;
import java.util.List;
import java.util.Objects;

/**
 * SQL Server DDL 生成器。
 */
public class SqlServerDdlGenerator implements DdlGenerator {

  /** 字段类型映射器。 */
  private final FieldTypeMapper fieldTypeMapper;

  /**
   * 创建 SQL Server DDL 生成器。
   *
   * @param fieldTypeMapper 字段类型映射器
   */
  public SqlServerDdlGenerator(FieldTypeMapper fieldTypeMapper) {
    this.fieldTypeMapper = Objects.requireNonNull(fieldTypeMapper, "字段类型映射器不能为空");
  }

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
   * 生成 SQL Server 创建数据表 DDL。
   *
   * @param table 低代码数据表模型
   * @return SQL Server 创建数据表 DDL
   */
  @Override
  public String generateCreateTable(LowcodeTable table) {
    List<LowcodeColumn> columns = table.getColumns();
    StringBuilder ddl = new StringBuilder();
    ddl.append("IF OBJECT_ID(N'")
        .append(table.getName())
        .append("', N'U') IS NULL\nBEGIN\nCREATE TABLE ")
        .append(quote(table.getName()))
        .append(" (\n");
    for (int i = 0; i < columns.size(); i++) {
      ddl.append("    ").append(columnDefinition(columns.get(i))).append(",\n");
    }
    ddl.append("    CONSTRAINT ")
        .append(quote("pk_" + table.getName()))
        .append(" PRIMARY KEY (")
        .append(DdlGenerationSupport.primaryKeys(columns, SqlServerDdlGenerator::quote))
        .append(")\n);\nEND;");
    appendComments(ddl, table, columns);
    appendTenantDeletedIndex(ddl, table, columns);
    return ddl.toString();
  }

  private String columnDefinition(LowcodeColumn column) {
    StringBuilder definition = new StringBuilder();
    definition.append(quote(column.getName()))
        .append(" ")
        .append(fieldTypeMapper.toDatabaseType(column));
    if (column.isAutoIncrement()) {
      definition.append(" IDENTITY(1,1)");
    }
    definition.append(column.isNullable() ? " NULL" : " NOT NULL");
    String defaultClause = commonColumnDefaultClause(column);
    if (DdlGenerationSupport.hasText(defaultClause)) {
      definition.append(" ").append(defaultClause);
    }
    return definition.toString();
  }

  private static void appendComments(StringBuilder ddl, LowcodeTable table, List<LowcodeColumn> columns) {
    if (DdlGenerationSupport.hasText(table.getComment())) {
      ddl.append("\nEXEC sp_addextendedproperty N'MS_Description', N'")
          .append(DdlGenerationSupport.escapeComment(table.getComment()))
          .append("', N'SCHEMA', N'dbo', N'TABLE', N'")
          .append(table.getName())
          .append("';");
    }
    for (LowcodeColumn column : columns) {
      if (DdlGenerationSupport.hasText(column.getComment())) {
        ddl.append("\nEXEC sp_addextendedproperty N'MS_Description', N'")
            .append(DdlGenerationSupport.escapeComment(column.getComment()))
            .append("', N'SCHEMA', N'dbo', N'TABLE', N'")
            .append(table.getName())
            .append("', N'COLUMN', N'")
            .append(column.getName())
            .append("';");
      }
    }
  }

  private static void appendTenantDeletedIndex(StringBuilder ddl, LowcodeTable table, List<LowcodeColumn> columns) {
    if (!DdlGenerationSupport.needsTenantDeletedIndex(columns)) {
      return;
    }
    String indexName = DdlGenerationSupport.tenantDeletedIndexName(table.getName());
    ddl.append("\nIF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'")
        .append(indexName)
        .append("' AND object_id = OBJECT_ID(N'")
        .append(table.getName())
        .append("'))\nCREATE INDEX ")
        .append(quote(indexName))
        .append(" ON ")
        .append(quote(table.getName()))
        .append(" (")
        .append(quote("tenant_id"))
        .append(", ")
        .append(quote("deleted"))
        .append(");");
  }

  private static String commonColumnDefaultClause(LowcodeColumn column) {
    return switch (column.getName().toLowerCase()) {
      case "created_at", "updated_at" -> "DEFAULT SYSDATETIME()";
      case "deleted", "version" -> "DEFAULT 0";
      default -> "";
    };
  }

  private static String quote(String identifier) {
    return "[" + DdlGenerationSupport.requireIdentifier(identifier) + "]";
  }
}
