/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.schema;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.secret.SecretReference;
import com.zhyc.common.secret.SecretResolver;
import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodePhysicalColumn;
import com.zhyc.lowcode.metadata.domain.LowcodePhysicalTable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * 基于 JDBC 的低代码数据库结构访问网关。
 *
 * <p>运行期只通过密钥引用解析数据库口令，不接受或记录明文口令；所有数据库异常转换为稳定业务异常返回后台。</p>
 */
@Component
public class JdbcLowcodeDatabaseSchemaGateway implements LowcodeDatabaseSchemaGateway {

  /** 密钥解析器缺失错误码。 */
  private static final String ERROR_SECRET_RESOLVER_MISSING = "ZHYC_LOWCODE_SCHEMA_SECRET_RESOLVER_MISSING";
  /** 数据源未启用错误码。 */
  private static final String ERROR_DATASOURCE_DISABLED = "ZHYC_LOWCODE_SCHEMA_DATASOURCE_DISABLED";
  /** 表名缺失错误码。 */
  private static final String ERROR_TABLE_NAME_REQUIRED = "ZHYC_LOWCODE_SCHEMA_TABLE_NAME_REQUIRED";
  /** 表名非法错误码。 */
  private static final String ERROR_TABLE_NAME_INVALID = "ZHYC_LOWCODE_SCHEMA_TABLE_NAME_INVALID";
  /** 表不存在错误码。 */
  private static final String ERROR_TABLE_NOT_FOUND = "ZHYC_LOWCODE_SCHEMA_TABLE_NOT_FOUND";
  /** 数据库结构读取失败错误码。 */
  private static final String ERROR_SCHEMA_READ_FAILED = "ZHYC_LOWCODE_SCHEMA_READ_FAILED";
  /** DDL 执行失败错误码。 */
  private static final String ERROR_DDL_EXECUTE_FAILED = "ZHYC_LOWCODE_SCHEMA_DDL_EXECUTE_FAILED";
  /** 数据库口令密钥引用缺失错误码。 */
  private static final String ERROR_PASSWORD_SECRET_REQUIRED = "ZHYC_LOWCODE_SCHEMA_PASSWORD_SECRET_REQUIRED";

  /** 数据库口令密钥解析器。 */
  private final Optional<SecretResolver> secretResolver;

  /**
   * 创建 JDBC 数据库结构网关。
   *
   * @param secretResolver 数据库口令密钥解析器，可在未接入密钥中心时为空
   */
  public JdbcLowcodeDatabaseSchemaGateway(Optional<SecretResolver> secretResolver) {
    this.secretResolver = secretResolver == null ? Optional.empty() : secretResolver;
  }

  @Override
  public List<LowcodePhysicalTable> listTables(LowcodeDataSource dataSource) {
    ensureEnabled(dataSource);
    try (Connection connection = openConnection(dataSource)) {
      DatabaseMetaData metaData = connection.getMetaData();
      List<LowcodePhysicalTable> tables = new ArrayList<>();
      try (ResultSet resultSet = metaData.getTables(connection.getCatalog(), null, "%", new String[] {"TABLE"})) {
        while (resultSet.next()) {
          String tableName = resultSet.getString("TABLE_NAME");
          String remarks = resultSet.getString("REMARKS");
          tables.add(new LowcodePhysicalTable(tableName, remarks, List.of()));
        }
      }
      return tables;
    } catch (SQLException exception) {
      throw new BusinessException(ERROR_SCHEMA_READ_FAILED, "读取数据库表清单失败: " + exception.getMessage());
    }
  }

  @Override
  public LowcodePhysicalTable readTable(LowcodeDataSource dataSource, String tableName) {
    ensureEnabled(dataSource);
    String normalizedTableName = requireIdentifier(tableName);
    try (Connection connection = openConnection(dataSource)) {
      DatabaseMetaData metaData = connection.getMetaData();
      String tableComment = findTableComment(connection, metaData, normalizedTableName);
      Set<String> primaryKeys = loadPrimaryKeys(connection, metaData, normalizedTableName);
      List<LowcodePhysicalColumn> columns = loadColumns(connection, metaData, normalizedTableName, primaryKeys);
      return new LowcodePhysicalTable(normalizedTableName, tableComment, columns);
    } catch (SQLException exception) {
      throw new BusinessException(ERROR_SCHEMA_READ_FAILED, "读取数据库表结构失败: " + exception.getMessage());
    }
  }

  @Override
  public void executeDdl(LowcodeDataSource dataSource, String ddl) {
    ensureEnabled(dataSource);
    try (Connection connection = openConnection(dataSource);
         Statement statement = connection.createStatement()) {
      statement.execute(ddl);
    } catch (SQLException exception) {
      throw new BusinessException(ERROR_DDL_EXECUTE_FAILED, "执行建表 DDL 失败: " + exception.getMessage());
    }
  }

  private Connection openConnection(LowcodeDataSource dataSource) throws SQLException {
    Properties properties = new Properties();
    properties.setProperty("user", dataSource.getUsername());
    properties.setProperty("password", resolvePassword(dataSource));
    DriverManager.setLoginTimeout(10);
    return DriverManager.getConnection(dataSource.getJdbcUrl(), properties);
  }

  private String resolvePassword(LowcodeDataSource dataSource) {
    String passwordSecretRef = dataSource.getPasswordSecretRef();
    if (passwordSecretRef == null || passwordSecretRef.trim().isEmpty()) {
      throw new BusinessException(ERROR_PASSWORD_SECRET_REQUIRED, "数据库口令密钥引用不能为空");
    }
    SecretResolver resolver = secretResolver.orElseThrow(() ->
        new BusinessException(ERROR_SECRET_RESOLVER_MISSING, "未配置密钥解析器，不能连接低代码数据源"));
    try {
      return resolver.resolve(SecretReference.parse(passwordSecretRef));
    } catch (IllegalArgumentException exception) {
      throw new BusinessException(ERROR_PASSWORD_SECRET_REQUIRED, exception.getMessage());
    }
  }

  private static void ensureEnabled(LowcodeDataSource dataSource) {
    if (!dataSource.isEnabled()) {
      throw new BusinessException(ERROR_DATASOURCE_DISABLED, "数据源未启用，不能读取表结构或执行 DDL");
    }
  }

  private static String findTableComment(Connection connection, DatabaseMetaData metaData, String tableName)
      throws SQLException {
    try (ResultSet resultSet = metaData.getTables(connection.getCatalog(), null, tableName, new String[] {"TABLE"})) {
      if (!resultSet.next()) {
        throw new BusinessException(ERROR_TABLE_NOT_FOUND, "数据源中不存在物理表: " + tableName);
      }
      return resultSet.getString("REMARKS");
    }
  }

  private static Set<String> loadPrimaryKeys(Connection connection, DatabaseMetaData metaData, String tableName)
      throws SQLException {
    Set<String> primaryKeys = new HashSet<>();
    try (ResultSet resultSet = metaData.getPrimaryKeys(connection.getCatalog(), null, tableName)) {
      while (resultSet.next()) {
        primaryKeys.add(resultSet.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
      }
    }
    return primaryKeys;
  }

  private static List<LowcodePhysicalColumn> loadColumns(Connection connection, DatabaseMetaData metaData,
                                                        String tableName, Set<String> primaryKeys)
      throws SQLException {
    List<LowcodePhysicalColumn> columns = new ArrayList<>();
    try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, tableName, "%")) {
      while (resultSet.next()) {
        String columnName = resultSet.getString("COLUMN_NAME");
        int sqlType = resultSet.getInt("DATA_TYPE");
        LowcodePhysicalColumn.Builder builder = LowcodePhysicalColumn.builder(columnName, mapFieldType(sqlType))
            .length(resolveLength(sqlType, resultSet.getInt("COLUMN_SIZE")))
            .scale(resolveScale(sqlType, resultSet.getInt("DECIMAL_DIGITS")))
            .nullable(resultSet.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls)
            .primaryKey(primaryKeys.contains(columnName.toLowerCase(Locale.ROOT)))
            .autoIncrement("YES".equalsIgnoreCase(resultSet.getString("IS_AUTOINCREMENT")))
            .comment(resultSet.getString("REMARKS"));
        columns.add(builder.build());
      }
    }
    if (columns.isEmpty()) {
      throw new BusinessException(ERROR_TABLE_NOT_FOUND, "数据源中不存在物理表字段: " + tableName);
    }
    return columns;
  }

  private static LowcodeFieldType mapFieldType(int sqlType) {
    return switch (sqlType) {
      case Types.CHAR, Types.NCHAR, Types.VARCHAR, Types.NVARCHAR -> LowcodeFieldType.STRING;
      case Types.LONGVARCHAR, Types.LONGNVARCHAR, Types.CLOB, Types.NCLOB -> LowcodeFieldType.TEXT;
      case Types.TINYINT, Types.SMALLINT, Types.INTEGER -> LowcodeFieldType.INTEGER;
      case Types.BIGINT -> LowcodeFieldType.LONG;
      case Types.DECIMAL, Types.NUMERIC, Types.DOUBLE, Types.FLOAT, Types.REAL -> LowcodeFieldType.DECIMAL;
      case Types.BIT, Types.BOOLEAN -> LowcodeFieldType.BOOLEAN;
      case Types.DATE -> LowcodeFieldType.DATE;
      case Types.TIME, Types.TIME_WITH_TIMEZONE, Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE ->
          LowcodeFieldType.DATETIME;
      default -> LowcodeFieldType.STRING;
    };
  }

  private static Integer resolveLength(int sqlType, int columnSize) {
    if (columnSize <= 0) {
      return null;
    }
    return switch (mapFieldType(sqlType)) {
      case STRING, DECIMAL -> columnSize;
      default -> null;
    };
  }

  private static Integer resolveScale(int sqlType, int decimalDigits) {
    if (decimalDigits < 0 || mapFieldType(sqlType) != LowcodeFieldType.DECIMAL) {
      return null;
    }
    return decimalDigits;
  }

  private static String requireIdentifier(String tableName) {
    if (tableName == null || tableName.trim().isEmpty()) {
      throw new BusinessException(ERROR_TABLE_NAME_REQUIRED, "物理表名不能为空");
    }
    String normalized = tableName.trim();
    if (!normalized.matches("[A-Za-z_][A-Za-z0-9_]*")) {
      throw new BusinessException(ERROR_TABLE_NAME_INVALID, "物理表名只能包含字母、数字和下划线: " + normalized);
    }
    return normalized;
  }
}
