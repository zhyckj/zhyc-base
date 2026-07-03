/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * 应用内 SQL 运行监控采集器。
 *
 * <p>该采集器聚合 MyBatis 实际执行过的 SQL 摘要和耗时，用于弥补数据库性能视图未开启、
 * 账号无权限或慢 SQL 阈值过高时 SQL 监控页面没有内容的问题。</p>
 */
@Component
public class RuntimeSqlMonitorCollector {

  /** 默认保留的 SQL 摘要数量。 */
  private static final int DEFAULT_MAX_DIGEST_SIZE = 300;
  /** 字符串字面量匹配模式。 */
  private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("'(?:''|[^'])*'");
  /** 数值字面量匹配模式。 */
  private static final Pattern NUMERIC_LITERAL_PATTERN = Pattern.compile("\\b\\d+(?:\\.\\d+)?\\b");
  /** SQL 空白字符匹配模式。 */
  private static final Pattern SQL_WHITESPACE_PATTERN = Pattern.compile("\\s+");

  /** SQL 监控分析器。 */
  private final RuntimeSqlMonitorAnalyzer analyzer = new RuntimeSqlMonitorAnalyzer();
  /** 最大保留 SQL 摘要数量。 */
  private final int maxDigestSize;
  /** SQL 摘要聚合结果。 */
  private final ConcurrentMap<String, SqlExecutionAggregate> aggregates = new ConcurrentHashMap<>();
  /** SQL 摘要插入顺序，用于超过容量时淘汰旧记录。 */
  private final ConcurrentLinkedDeque<String> aggregateKeys = new ConcurrentLinkedDeque<>();

  /**
   * 创建默认 SQL 运行监控采集器。
   */
  public RuntimeSqlMonitorCollector() {
    this(DEFAULT_MAX_DIGEST_SIZE);
  }

  /**
   * 创建 SQL 运行监控采集器。
   *
   * @param maxDigestSize 最大保留 SQL 摘要数量
   */
  RuntimeSqlMonitorCollector(int maxDigestSize) {
    this.maxDigestSize = Math.max(10, maxDigestSize);
  }

  /**
   * 记录一次 SQL 执行。
   *
   * @param sourceCode 数据源编码
   * @param rawSql 原始 SQL
   * @param costMs 执行耗时，单位毫秒
   * @param rowsSent 返回或影响行数
   */
  public void record(String sourceCode, String rawSql, long costMs, long rowsSent) {
    String normalizedSql = normalizeSql(rawSql);
    if (normalizedSql.isEmpty()) {
      return;
    }
    String normalizedSourceCode = normalizeSourceCode(sourceCode);
    String aggregateKey = normalizedSourceCode + "|" + normalizedSql.toLowerCase(Locale.ROOT);
    SqlExecutionAggregate aggregate = aggregates.computeIfAbsent(aggregateKey, key -> {
      aggregateKeys.addLast(key);
      return new SqlExecutionAggregate(normalizedSourceCode, normalizedSql);
    });
    trimOverflow();
    aggregate.record(Math.max(1L, costMs), Math.max(0L, rowsSent));
  }

  /**
   * 查询已采集 SQL 监控记录。
   *
   * @param thresholdMs 最小平均耗时阈值，单位毫秒
   * @param limit 最大返回记录数
   * @return SQL 监控记录
   */
  public List<RuntimeSqlMonitorRecord> listRecords(int thresholdMs, int limit) {
    int normalizedThresholdMs = Math.min(Math.max(thresholdMs, 1), 60_000);
    int normalizedLimit = Math.min(Math.max(limit, 1), 100);
    return aggregates.values().stream()
        .map(this::toMonitorRecord)
        .filter(record -> record.avgCostMs() >= normalizedThresholdMs)
        .sorted(Comparator
            .comparingLong(RuntimeSqlMonitorRecord::maxCostMs).reversed()
            .thenComparing(RuntimeSqlMonitorRecord::lastSeen, Comparator.reverseOrder()))
        .limit(normalizedLimit)
        .toList();
  }

  /**
   * 将聚合数据转换为监控记录。
   *
   * @param aggregate SQL 聚合数据
   * @return SQL 监控记录
   */
  private RuntimeSqlMonitorRecord toMonitorRecord(SqlExecutionAggregate aggregate) {
    long executeCount = aggregate.executeCount.get();
    long rowsSent = aggregate.rowsSent.get();
    long avgCostMs = executeCount == 0 ? 0L : Math.max(1L, aggregate.totalCostMs.get() / executeCount);
    return analyzer.analyze(aggregate.sourceCode, aggregate.sqlDigest, executeCount, avgCostMs,
        aggregate.maxCostMs.get(), rowsSent, rowsSent, aggregate.lastSeen.toString());
  }

  /**
   * 超过容量时淘汰最早的聚合记录。
   */
  private void trimOverflow() {
    while (aggregates.size() > maxDigestSize) {
      String oldestKey = aggregateKeys.pollFirst();
      if (oldestKey == null) {
        return;
      }
      aggregates.remove(oldestKey);
    }
  }

  /**
   * 标准化数据源编码。
   *
   * @param sourceCode 原始数据源编码
   * @return 数据源编码
   */
  private static String normalizeSourceCode(String sourceCode) {
    if (sourceCode == null || sourceCode.trim().isEmpty()) {
      return "dataSource";
    }
    return sourceCode.trim();
  }

  /**
   * 标准化 SQL 摘要。
   *
   * @param rawSql 原始 SQL
   * @return 归一化 SQL 摘要
   */
  private static String normalizeSql(String rawSql) {
    if (rawSql == null || rawSql.trim().isEmpty()) {
      return "";
    }
    String withoutStringLiteral = STRING_LITERAL_PATTERN.matcher(rawSql).replaceAll("?");
    String withoutNumericLiteral = NUMERIC_LITERAL_PATTERN.matcher(withoutStringLiteral).replaceAll("?");
    return SQL_WHITESPACE_PATTERN.matcher(withoutNumericLiteral.trim()).replaceAll(" ");
  }

  /**
   * SQL 执行聚合数据。
   */
  private static final class SqlExecutionAggregate {

    /** 数据源编码。 */
    private final String sourceCode;
    /** SQL 摘要。 */
    private final String sqlDigest;
    /** 执行次数。 */
    private final AtomicLong executeCount = new AtomicLong();
    /** 总耗时，单位毫秒。 */
    private final AtomicLong totalCostMs = new AtomicLong();
    /** 最大耗时，单位毫秒。 */
    private final AtomicLong maxCostMs = new AtomicLong();
    /** 返回或影响总行数。 */
    private final AtomicLong rowsSent = new AtomicLong();
    /** 最近一次采集时间。 */
    private volatile LocalDateTime lastSeen = LocalDateTime.now();

    /**
     * 创建 SQL 执行聚合数据。
     *
     * @param sourceCode 数据源编码
     * @param sqlDigest SQL 摘要
     */
    private SqlExecutionAggregate(String sourceCode, String sqlDigest) {
      this.sourceCode = Objects.requireNonNull(sourceCode, "数据源编码不能为空");
      this.sqlDigest = Objects.requireNonNull(sqlDigest, "SQL 摘要不能为空");
    }

    /**
     * 记录一次执行。
     *
     * @param costMs 执行耗时，单位毫秒
     * @param rows 返回或影响行数
     */
    private void record(long costMs, long rows) {
      executeCount.incrementAndGet();
      totalCostMs.addAndGet(costMs);
      maxCostMs.accumulateAndGet(costMs, Math::max);
      rowsSent.addAndGet(rows);
      lastSeen = LocalDateTime.now();
    }
  }
}
