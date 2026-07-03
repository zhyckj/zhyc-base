/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 默认平台运行监控服务。
 */
@Service
public class DefaultRuntimeMonitorService implements RuntimeMonitorService {

  /** Spring 容器中注册的数据源集合，key 为 Bean 名称。 */
  private final Map<String, DataSource> dataSources;
  /** 当前应用名称。 */
  private final String applicationName;
  /** 当前应用版本。 */
  private final String applicationVersion;
  /** 应用内 SQL 监控采集器。 */
  private final RuntimeSqlMonitorCollector sqlMonitorCollector;
  /** SQL 监控分析器。 */
  private final RuntimeSqlMonitorAnalyzer sqlMonitorAnalyzer = new RuntimeSqlMonitorAnalyzer();

  /**
   * 创建默认平台运行监控服务。
   *
   * @param dataSources Spring 容器中注册的数据源集合
   * @param applicationName 当前应用名称
   * @param applicationVersion 当前应用版本
   */
  @Autowired
  public DefaultRuntimeMonitorService(Map<String, DataSource> dataSources,
                                      @Value("${spring.application.name:zhyc-platform-app}") String applicationName,
                                      @Value("${info.app.version:0.0.1-SNAPSHOT}") String applicationVersion,
                                      RuntimeSqlMonitorCollector sqlMonitorCollector) {
    this.dataSources = new LinkedHashMap<>(Objects.requireNonNull(dataSources, "数据源集合不能为空"));
    this.applicationName = applicationName;
    this.applicationVersion = applicationVersion;
    this.sqlMonitorCollector = Objects.requireNonNull(sqlMonitorCollector, "SQL 监控采集器不能为空");
  }

  /**
   * 创建默认平台运行监控服务。
   *
   * @param dataSources Spring 容器中注册的数据源集合
   * @param applicationName 当前应用名称
   * @param applicationVersion 当前应用版本
   */
  DefaultRuntimeMonitorService(Map<String, DataSource> dataSources, String applicationName, String applicationVersion) {
    this(dataSources, applicationName, applicationVersion, new RuntimeSqlMonitorCollector());
  }

  @Override
  public List<RuntimeServiceStatus> listServiceStatus() {
    long start = System.nanoTime();
    LocalDateTime heartbeatAt = LocalDateTime.now();
    long responseTimeMs = Math.max(1L, (System.nanoTime() - start) / 1_000_000L);
    return List.of(new RuntimeServiceStatus(applicationName, "UP", applicationVersion, responseTimeMs, heartbeatAt));
  }

  @Override
  public List<RuntimeDataSourceStatus> listDataSourceStatus() {
    return dataSources.entrySet().stream()
        .map(entry -> buildDataSourceStatus(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public List<RuntimeSqlMonitorRecord> listSqlMonitorRecords(int thresholdMs, int limit) {
    int normalizedThresholdMs = Math.min(Math.max(thresholdMs, 1), 60_000);
    int normalizedLimit = Math.min(Math.max(limit, 1), 100);
    List<RuntimeSqlMonitorRecord> collectedRecords = sqlMonitorCollector.listRecords(normalizedThresholdMs,
        normalizedLimit);
    List<RuntimeSqlMonitorRecord> performanceSchemaRecords = dataSources.entrySet().stream()
        .flatMap(entry -> querySqlMonitorRecords(entry.getKey(), entry.getValue(), normalizedThresholdMs,
            normalizedLimit).stream())
        .collect(Collectors.toList());
    return mergeSqlMonitorRecords(collectedRecords, performanceSchemaRecords, normalizedLimit);
  }

  /**
   * 构建单个数据源运行状态。
   *
   * @param sourceCode 数据源 Bean 名称
   * @param source 数据源实例
   * @return 数据源运行状态
   */
  private RuntimeDataSourceStatus buildDataSourceStatus(String sourceCode, DataSource source) {
    long start = System.nanoTime();
    LocalDateTime checkedAt = LocalDateTime.now();
    String status = isDataSourceAvailable(source) ? "CONNECTED" : "DISCONNECTED";
    long costMs = Math.max(1L, (System.nanoTime() - start) / 1_000_000L);
    return new RuntimeDataSourceStatus(sourceCode, resolveDataSourceName(sourceCode), status, costMs, checkedAt);
  }

  /**
   * 检测数据源连接是否可用。
   *
   * @param source 数据源实例
   * @return 连接可用时返回 {@code true}
   */
  private boolean isDataSourceAvailable(DataSource source) {
    try (Connection connection = source.getConnection()) {
      return connection != null && connection.isValid(1);
    } catch (SQLException ex) {
      return false;
    }
  }

  /**
   * 解析数据源显示名称。
   *
   * @param sourceCode 数据源 Bean 名称
   * @return 数据源显示名称
   */
  private String resolveDataSourceName(String sourceCode) {
    if (sourceCode.endsWith("DataSource") && sourceCode.length() > "DataSource".length()) {
      return sourceCode.substring(0, sourceCode.length() - "DataSource".length());
    }
    return sourceCode;
  }

  /**
   * 查询单个数据源的 SQL 执行效率记录。
   *
   * @param sourceCode 数据源编码
   * @param source 数据源实例
   * @param thresholdMs 慢 SQL 平均耗时阈值，单位毫秒
   * @param limit 最大返回记录数
   * @return SQL 执行效率记录列表
   */
  private List<RuntimeSqlMonitorRecord> querySqlMonitorRecords(String sourceCode, DataSource source,
      int thresholdMs, int limit) {
    try (Connection connection = source.getConnection()) {
      if (!supportsPerformanceSchema(connection)) {
        return List.of(unavailableRecord(sourceCode, "当前数据源暂不支持 SQL 性能视图监控"));
      }
      return queryMySqlStatementDigest(sourceCode, connection, thresholdMs, limit);
    } catch (SQLException ex) {
      return List.of(unavailableRecord(sourceCode, "SQL 性能视图不可用，请确认 performance_schema 已启用且账号具备查询权限"));
    }
  }

  private boolean supportsPerformanceSchema(Connection connection) throws SQLException {
    String productName = connection.getMetaData().getDatabaseProductName();
    String normalizedProductName = productName == null ? "" : productName.toLowerCase(Locale.ROOT);
    return normalizedProductName.contains("mysql") || normalizedProductName.contains("mariadb");
  }

  private List<RuntimeSqlMonitorRecord> queryMySqlStatementDigest(String sourceCode, Connection connection,
      int thresholdMs, int limit) throws SQLException {
    String sql = """
        SELECT DIGEST_TEXT,
               COUNT_STAR,
               ROUND(AVG_TIMER_WAIT / 1000000000) AS avgCostMs,
               ROUND(MAX_TIMER_WAIT / 1000000000) AS maxCostMs,
               SUM_ROWS_EXAMINED,
               SUM_ROWS_SENT,
               LAST_SEEN
        FROM performance_schema.events_statements_summary_by_digest
        WHERE DIGEST_TEXT IS NOT NULL
          AND SCHEMA_NAME IS NOT NULL
          AND SCHEMA_NAME NOT IN ('mysql', 'performance_schema', 'information_schema', 'sys')
          AND ROUND(AVG_TIMER_WAIT / 1000000000) >= ?
        ORDER BY AVG_TIMER_WAIT DESC
        LIMIT ?
        """;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, thresholdMs);
      statement.setInt(2, limit);
      try (ResultSet resultSet = statement.executeQuery()) {
        List<RuntimeSqlMonitorRecord> records = new ArrayList<>();
        while (resultSet.next()) {
          records.add(sqlMonitorAnalyzer.analyze(sourceCode, resultSet.getString("DIGEST_TEXT"),
              resultSet.getLong("COUNT_STAR"), resultSet.getLong("avgCostMs"),
              resultSet.getLong("maxCostMs"), resultSet.getLong("SUM_ROWS_EXAMINED"),
              resultSet.getLong("SUM_ROWS_SENT"), resultSet.getString("LAST_SEEN")));
        }
        return records;
      }
    }
  }

  private RuntimeSqlMonitorRecord unavailableRecord(String sourceCode, String suggestion) {
    return new RuntimeSqlMonitorRecord(sourceCode, "SQL 性能视图不可用", 0L, 0L, 0L, 0L, 0L,
        "UNAVAILABLE", suggestion, LocalDateTime.now().toString());
  }

  /**
   * 合并应用内采集和数据库性能视图记录。
   *
   * @param collectedRecords 应用内采集记录
   * @param performanceSchemaRecords 数据库性能视图记录
   * @param limit 最大返回记录数
   * @return SQL 监控记录
   */
  private static List<RuntimeSqlMonitorRecord> mergeSqlMonitorRecords(
      List<RuntimeSqlMonitorRecord> collectedRecords,
      List<RuntimeSqlMonitorRecord> performanceSchemaRecords,
      int limit) {
    Map<String, RuntimeSqlMonitorRecord> mergedRecords = new LinkedHashMap<>();
    collectedRecords.forEach(record -> mergedRecords.put(sqlMonitorRecordKey(record), record));
    performanceSchemaRecords.stream()
        .filter(record -> !"UNAVAILABLE".equals(record.severity()) || collectedRecords.isEmpty())
        .forEach(record -> mergedRecords.putIfAbsent(sqlMonitorRecordKey(record), record));
    return mergedRecords.values().stream()
        .sorted(Comparator
            .comparingLong(RuntimeSqlMonitorRecord::maxCostMs).reversed()
            .thenComparing(RuntimeSqlMonitorRecord::lastSeen, Comparator.nullsLast(Comparator.reverseOrder())))
        .limit(limit)
        .toList();
  }

  /**
   * 构建 SQL 监控记录去重键。
   *
   * @param record SQL 监控记录
   * @return 去重键
   */
  private static String sqlMonitorRecordKey(RuntimeSqlMonitorRecord record) {
    return record.sourceCode() + "|" + record.sqlDigest();
  }
}
