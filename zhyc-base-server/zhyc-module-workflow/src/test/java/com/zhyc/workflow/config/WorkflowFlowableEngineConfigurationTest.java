/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.lang.reflect.Proxy;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.junit.jupiter.api.Test;

/**
 * Flowable 引擎配置测试。
 */
class WorkflowFlowableEngineConfigurationTest {

  /**
   * 验证数据库不存在 Flowable 属性表时，配置会清理残表后切换为 drop-create 自愈策略。
   */
  @Test
  void shouldCleanupBrokenTablesAndUseCreateWhenFlowablePropertyTableMissing() {
    WorkflowFlowableEngineConfiguration configuration = new WorkflowFlowableEngineConfiguration();
    RecordingDataSource dataSource = new RecordingDataSource(false,
        List.of("ACT_RU_TASK", "ACT_HI_IDENTITYLINK", "sys_user"));

    String strategy = configuration.resolveSchemaUpdateStrategy(dataSource.proxy());

    assertEquals(AbstractEngineConfiguration.DB_SCHEMA_UPDATE_DROP_CREATE, strategy);
    assertIterableEquals(List.of(
        "SET FOREIGN_KEY_CHECKS = 0",
        "DROP TABLE IF EXISTS `ACT_RU_TASK`",
        "DROP TABLE IF EXISTS `ACT_HI_IDENTITYLINK`",
        "SET FOREIGN_KEY_CHECKS = 1"), dataSource.executedSql);
  }

  /**
   * 验证数据库已存在 Flowable 属性表时，配置会保留自动升级策略。
   */
  @Test
  void shouldUseTrueWhenFlowablePropertyTableExists() {
    WorkflowFlowableEngineConfiguration configuration = new WorkflowFlowableEngineConfiguration();
    RecordingDataSource dataSource = new RecordingDataSource(true,
        List.of("ACT_GE_PROPERTY", "ACT_RU_TASK"));

    String strategy = configuration.resolveSchemaUpdateStrategy(dataSource.proxy());

    assertEquals(AbstractEngineConfiguration.DB_SCHEMA_UPDATE_TRUE, strategy);
    assertIterableEquals(List.of(), dataSource.executedSql);
  }

  /**
   * 记录测试数据源交互。
   */
  private final class RecordingDataSource {

    /** 是否存在 Flowable 属性表。 */
    private final boolean propertyTableExists;
    /** 数据库中当前存在的表。 */
    private final List<String> tables;
    /** 已执行 SQL。 */
    private final List<String> executedSql = new ArrayList<>();

    private RecordingDataSource(boolean propertyTableExists, List<String> tables) {
      this.propertyTableExists = propertyTableExists;
      this.tables = tables;
    }

    /**
     * 创建测试数据源代理。
     *
     * @return 数据源代理
     */
    private DataSource proxy() {
      Connection connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
          new Class<?>[] {Connection.class}, (proxy, method, args) -> {
            if ("getMetaData".equals(method.getName())) {
              return databaseMetaData();
            }
            if ("getCatalog".equals(method.getName())) {
              return "zhyc-base-v1";
            }
            if ("createStatement".equals(method.getName())) {
              return statement();
            }
            if ("close".equals(method.getName())) {
              return null;
            }
            return defaultValue(method.getReturnType());
          });
      return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(),
          new Class<?>[] {DataSource.class}, (proxy, method, args) -> {
            if ("getConnection".equals(method.getName())) {
              return connection;
            }
            return defaultValue(method.getReturnType());
          });
    }

    /**
     * 创建测试数据库元数据。
     *
     * @return 元数据代理
     */
    private DatabaseMetaData databaseMetaData() {
      return (DatabaseMetaData) Proxy.newProxyInstance(DatabaseMetaData.class.getClassLoader(),
          new Class<?>[] {DatabaseMetaData.class}, (proxy, method, args) -> {
            if ("getTables".equals(method.getName())) {
              String tablePattern = args != null && args.length >= 3 ? String.valueOf(args[2]) : "%";
              return resultSet(tablePattern);
            }
            return defaultValue(method.getReturnType());
          });
    }

    /**
     * 创建测试语句代理。
     *
     * @return JDBC 语句代理
     */
    private Statement statement() {
      return (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(),
          new Class<?>[] {Statement.class}, (proxy, method, args) -> {
            if ("execute".equals(method.getName())) {
              executedSql.add(String.valueOf(args[0]));
              return true;
            }
            if ("close".equals(method.getName())) {
              return null;
            }
            return defaultValue(method.getReturnType());
          });
    }

    /**
     * 创建测试结果集。
     *
     * @param tablePattern 查询表名模式
     * @return 结果集代理
     */
    private ResultSet resultSet(String tablePattern) {
      List<String> matchedTables = matchedTables(tablePattern);
      return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(),
          new Class<?>[] {ResultSet.class}, new java.lang.reflect.InvocationHandler() {
            private int index = -1;

            @Override
            public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
              if ("next".equals(method.getName())) {
                index++;
                return index < matchedTables.size();
              }
              if ("getString".equals(method.getName()) && args != null && args.length == 1
                  && "TABLE_NAME".equals(args[0])) {
                return matchedTables.get(index);
              }
              if ("close".equals(method.getName())) {
                return null;
              }
              return defaultValue(method.getReturnType());
            }
          });
    }

    /**
     * 根据模式匹配表名。
     *
     * @param tablePattern JDBC 元数据表名模式
     * @return 匹配后的表名列表
     */
    private List<String> matchedTables(String tablePattern) {
      if (WorkflowFlowableEngineConfiguration.FLOWABLE_PROPERTY_TABLE.equalsIgnoreCase(tablePattern)) {
        return propertyTableExists ? List.of(WorkflowFlowableEngineConfiguration.FLOWABLE_PROPERTY_TABLE)
            : List.of();
      }
      if ("%".equals(tablePattern)) {
        return tables;
      }
      return tables.stream()
          .filter(tableName -> tablePattern.equalsIgnoreCase(tableName))
          .toList();
    }
  }

  /**
   * 返回代理方法的默认返回值。
   *
   * @param returnType 返回类型
   * @return 默认返回值
   */
  private Object defaultValue(Class<?> returnType) {
    if (returnType == Void.TYPE) {
      return null;
    }
    if (returnType == Boolean.TYPE) {
      return false;
    }
    if (returnType == Integer.TYPE) {
      return 0;
    }
    if (returnType == Long.TYPE) {
      return 0L;
    }
    if (returnType == Double.TYPE) {
      return 0D;
    }
    if (returnType == Float.TYPE) {
      return 0F;
    }
    if (returnType == Short.TYPE) {
      return (short) 0;
    }
    if (returnType == Byte.TYPE) {
      return (byte) 0;
    }
    if (returnType == Character.TYPE) {
      return (char) 0;
    }
    return null;
  }
}
