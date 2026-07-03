/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.config;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 工作流 Flowable 引擎配置。
 *
 * <p>平台首期采用与核心平台同库部署的嵌入式 Flowable 引擎，避免本地开发和轻量部署时还需要单独配置
 * 外部 Flowable 发布服务。引擎默认自动建表；当数据库中不存在 Flowable 基准表时，会先清理残缺的
 * `ACT_*` 表，再按纯创建策略重建，兼容本地开发时遇到的半初始化或脏残留库。</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "zhyc.workflow.flowable", name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class WorkflowFlowableEngineConfiguration {

  /** 默认流程引擎名称。 */
  private static final String DEFAULT_ENGINE_NAME = "zhyc-flowable-engine";
  /** Flowable 公共属性表名。 */
  static final String FLOWABLE_PROPERTY_TABLE = "ACT_GE_PROPERTY";

  /**
   * 创建 Flowable Spring 引擎配置。
   *
   * @param dataSource 平台主数据源
   * @param transactionManager Spring 事务管理器
   * @return Flowable Spring 引擎配置
   */
  @Bean
  @ConditionalOnMissingBean(SpringProcessEngineConfiguration.class)
  public SpringProcessEngineConfiguration springProcessEngineConfiguration(DataSource dataSource,
      ObjectProvider<PlatformTransactionManager> transactionManager) {
    SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
    configuration.setDataSource(dataSource);
    configuration.setTransactionManager(transactionManager.getIfAvailable());
    configuration.setDatabaseSchemaUpdate(resolveSchemaUpdateStrategy(dataSource));
    configuration.setDatabaseCatalog(resolveDatabaseCatalog(dataSource));
    configuration.setEngineName(DEFAULT_ENGINE_NAME);
    configuration.setAsyncExecutorActivate(false);
    configuration.setDbHistoryUsed(true);
    configuration.setDisableIdmEngine(true);
    configuration.setDisableEventRegistry(true);
    return configuration;
  }

  /**
   * 解析 Flowable 建表策略。
   *
   * <p>当数据库中不存在 Flowable 公共属性表时，说明当前库没有完整可用的 Flowable 基础表。此时先按
   * 元数据清理残缺的 `ACT_*` 表，再走 `drop-create`。这是因为 Flowable 8 在 `create` 模式下，只要任一
   * 服务判断自己存在旧表，就会直接进入版本校验并把缺失属性表视为 `6.1.2.0` 旧库，最终抛出版本不匹配。
   * `drop-create` 对缺表容忍度更高，更适合本地首次启动和残库自愈。</p>
   *
   * @param dataSource 平台主数据源
   * @return Flowable 建表策略
   */
  String resolveSchemaUpdateStrategy(DataSource dataSource) {
    if (!hasFlowablePropertyTable(dataSource)) {
      cleanupBrokenFlowableTables(dataSource);
      return AbstractEngineConfiguration.DB_SCHEMA_UPDATE_DROP_CREATE;
    }
    return AbstractEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;
  }

  /**
   * 清理数据库中残留的 Flowable 运行表。
   *
   * <p>仅在 Flowable 属性表不存在时触发，表示当前库处于“表残缺但并未完成初始化”的状态。此时删除
   * 已存在的 `ACT_*` 表，让后续 `create` 可以从干净状态重建。</p>
   *
   * @param dataSource 平台主数据源
   */
  void cleanupBrokenFlowableTables(DataSource dataSource) {
    List<String> flowableTables = findFlowableTables(dataSource);
    if (flowableTables.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      executeSilently(statement, "SET FOREIGN_KEY_CHECKS = 0");
      for (String tableName : flowableTables) {
        statement.execute("DROP TABLE IF EXISTS `" + escapeTableName(tableName) + "`");
      }
      executeSilently(statement, "SET FOREIGN_KEY_CHECKS = 1");
    } catch (SQLException exception) {
      throw new IllegalStateException("清理残缺 Flowable 表失败", exception);
    }
  }

  /**
   * 查询数据库中已存在的 Flowable 表。
   *
   * @param dataSource 平台主数据源
   * @return Flowable 表名列表
   */
  List<String> findFlowableTables(DataSource dataSource) {
    List<String> tableNames = new ArrayList<>();
    try (Connection connection = dataSource.getConnection()) {
      if (supportsInformationSchema(connection)) {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT TABLE_NAME
            FROM information_schema.tables
            WHERE TABLE_SCHEMA = DATABASE()
              AND UPPER(TABLE_NAME) REGEXP '^(ACT|FLW)_'
            ORDER BY TABLE_NAME
            """);
             ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            tableNames.add(resultSet.getString(1));
          }
        }
        return tableNames;
      }
      DatabaseMetaData metadata = connection.getMetaData();
      if (metadata == null) {
        return tableNames;
      }
      try (ResultSet tables = metadata.getTables(connection.getCatalog(), null, "%",
          new String[] {"TABLE"})) {
        while (tables.next()) {
          String tableName = tables.getString("TABLE_NAME");
          if (tableName != null && (tableName.toUpperCase().startsWith("ACT_")
              || tableName.toUpperCase().startsWith("FLW_"))) {
            tableNames.add(tableName);
          }
        }
      }
      return tableNames;
    } catch (SQLException exception) {
      throw new IllegalStateException("查询 Flowable 残留表失败", exception);
    }
  }

  /**
   * 检查数据库中是否存在 Flowable 公共属性表。
   *
   * @param dataSource 平台主数据源
   * @return 存在返回 true，不存在或探测失败返回 false
   */
  boolean hasFlowablePropertyTable(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      if (supportsInformationSchema(connection)) {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT 1
            FROM information_schema.tables
            WHERE TABLE_SCHEMA = DATABASE()
              AND UPPER(TABLE_NAME) = ?
            """)) {
          statement.setString(1, FLOWABLE_PROPERTY_TABLE.toUpperCase());
          try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
          }
        }
      }
      DatabaseMetaData metadata = connection.getMetaData();
      if (metadata == null) {
        return false;
      }
      try (ResultSet directTables = metadata.getTables(connection.getCatalog(), null,
          FLOWABLE_PROPERTY_TABLE, new String[] {"TABLE"})) {
        if (directTables.next()) {
          return true;
        }
      }
      try (ResultSet upperTables = metadata.getTables(connection.getCatalog(), null,
          FLOWABLE_PROPERTY_TABLE.toUpperCase(), new String[] {"TABLE"})) {
        return upperTables.next();
      }
    } catch (SQLException exception) {
      return false;
    }
  }

  /**
   * 判断当前数据库连接是否支持 information_schema 探测。
   *
   * @param connection JDBC 连接
   * @return 支持返回 true
   */
  private boolean supportsInformationSchema(Connection connection) {
    try {
      DatabaseMetaData metadata = connection.getMetaData();
      if (metadata == null) {
        return false;
      }
      String productName = metadata.getDatabaseProductName();
      return productName != null && productName.toLowerCase().contains("mysql");
    } catch (SQLException exception) {
      return false;
    }
  }

  /**
   * 解析当前数据源绑定的数据库 catalog，避免 Flowable 元数据探测误扫到其他库的旧表。
   *
   * @param dataSource 平台主数据源
   * @return 当前 catalog，解析失败时返回 null
   */
  private String resolveDatabaseCatalog(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      return connection.getCatalog();
    } catch (SQLException exception) {
      return null;
    }
  }

  /**
   * 转义表名中的反引号，避免拼装删除语句时出现非法标识符。
   *
   * @param tableName 原始表名
   * @return 转义后的表名
   */
  private String escapeTableName(String tableName) {
    return tableName.replace("`", "``");
  }

  /**
   * 执行可忽略失败的 SQL。
   *
   * @param statement JDBC 语句
   * @param sql 待执行 SQL
   */
  private void executeSilently(Statement statement, String sql) {
    try {
      statement.execute(sql);
    } catch (SQLException exception) {
      // MySQL 之外的方言可能不支持该语句，本地自愈场景允许忽略。
    }
  }

  /**
   * 创建 Flowable 流程引擎工厂。
   *
   * @param configuration Flowable Spring 引擎配置
   * @return 流程引擎工厂
   */
  @Bean
  @ConditionalOnMissingBean
  public ProcessEngineFactoryBean processEngineFactoryBean(
      SpringProcessEngineConfiguration configuration) {
    ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
    factoryBean.setProcessEngineConfiguration(configuration);
    return factoryBean;
  }

  /**
   * 暴露 Flowable 流程引擎 Bean。
   *
   * @param factoryBean 流程引擎工厂
   * @return Flowable 流程引擎
   * @throws Exception 构建流程引擎失败时抛出
   */
  @Bean(destroyMethod = "close")
  @ConditionalOnMissingBean
  public ProcessEngine processEngine(ProcessEngineFactoryBean factoryBean) throws Exception {
    return factoryBean.getObject();
  }

  /**
   * 暴露 Flowable 仓储服务 Bean。
   *
   * @param processEngine Flowable 流程引擎
   * @return Flowable 仓储服务
   */
  @Bean
  @ConditionalOnMissingBean
  public RepositoryService repositoryService(ProcessEngine processEngine) {
    return processEngine.getRepositoryService();
  }

  /**
   * 暴露 Flowable 运行时服务 Bean。
   *
   * @param processEngine Flowable 流程引擎
   * @return Flowable 运行时服务
   */
  @Bean
  @ConditionalOnMissingBean
  public RuntimeService runtimeService(ProcessEngine processEngine) {
    return processEngine.getRuntimeService();
  }

  /**
   * 暴露 Flowable 任务服务 Bean。
   *
   * @param processEngine Flowable 流程引擎
   * @return Flowable 任务服务
   */
  @Bean
  @ConditionalOnMissingBean
  public TaskService taskService(ProcessEngine processEngine) {
    return processEngine.getTaskService();
  }

  /**
   * 暴露 Flowable 历史服务 Bean。
   *
   * @param processEngine Flowable 流程引擎
   * @return Flowable 历史服务
   */
  @Bean
  @ConditionalOnMissingBean
  public HistoryService historyService(ProcessEngine processEngine) {
    return processEngine.getHistoryService();
  }
}
