/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SQL 执行效率监控分析器。
 *
 * <p>根据性能视图采集到的 SQL 摘要和执行指标生成慢 SQL 等级与优化建议。</p>
 */
public class RuntimeSqlMonitorAnalyzer {

  /** 严重慢 SQL 阈值，单位毫秒。 */
  private static final long CRITICAL_AVG_COST_MS = 1_000L;
  /** 慢 SQL 阈值，单位毫秒。 */
  private static final long SLOW_AVG_COST_MS = 300L;
  /** 扫描行数异常放大倍数。 */
  private static final long ROW_SCAN_AMPLIFICATION = 100L;

  /**
   * 分析 SQL 执行指标。
   *
   * @param sourceCode 数据源编码
   * @param sqlDigest 归一化 SQL 摘要
   * @param executeCount 执行次数
   * @param avgCostMs 平均执行耗时，单位毫秒
   * @param maxCostMs 最大执行耗时，单位毫秒
   * @param rowsExamined 扫描行数
   * @param rowsSent 返回行数
   * @param lastSeen 最近一次采集时间
   * @return SQL 监控记录
   */
  public RuntimeSqlMonitorRecord analyze(String sourceCode, String sqlDigest, long executeCount, long avgCostMs,
      long maxCostMs, long rowsExamined, long rowsSent, String lastSeen) {
    String normalizedSql = normalizeSql(sqlDigest);
    return new RuntimeSqlMonitorRecord(sourceCode, normalizedSql, Math.max(0L, executeCount), Math.max(0L, avgCostMs),
        Math.max(0L, maxCostMs), Math.max(0L, rowsExamined), Math.max(0L, rowsSent),
        resolveSeverity(avgCostMs, rowsExamined, rowsSent), buildSuggestion(normalizedSql, avgCostMs, rowsExamined,
            rowsSent), lastSeen);
  }

  private String resolveSeverity(long avgCostMs, long rowsExamined, long rowsSent) {
    if (avgCostMs >= CRITICAL_AVG_COST_MS) {
      return "CRITICAL";
    }
    if (avgCostMs >= SLOW_AVG_COST_MS || hasScanAmplification(rowsExamined, rowsSent)) {
      return "SLOW";
    }
    return "NORMAL";
  }

  private String buildSuggestion(String sqlDigest, long avgCostMs, long rowsExamined, long rowsSent) {
    String lowerSql = sqlDigest.toLowerCase(Locale.ROOT);
    List<String> suggestions = new ArrayList<>();
    if (avgCostMs >= CRITICAL_AVG_COST_MS) {
      suggestions.add("平均耗时超过 1 秒，优先执行 EXPLAIN 检查执行计划、索引命中和临时表使用情况");
    }
    if (hasScanAmplification(rowsExamined, rowsSent)) {
      suggestions.add("扫描行数明显高于返回行数，建议补充匹配 WHERE 条件和排序字段的联合索引");
    }
    if (lowerSql.contains("select *")) {
      suggestions.add("避免 SELECT *，只查询页面或接口实际需要的字段");
    }
    if (lowerSql.contains(" order by ") && !lowerSql.contains(" limit ")) {
      suggestions.add("ORDER BY 未限制结果集，建议增加分页 LIMIT 或优化排序索引");
    }
    if (lowerSql.contains(" like ") && lowerSql.contains("%")) {
      suggestions.add("LIKE 前置通配可能导致索引失效，建议改用全文索引或搜索引擎");
    }
    if (suggestions.isEmpty()) {
      suggestions.add("建议结合业务调用频率、执行计划和索引选择性进一步确认优化空间");
    }
    return String.join("；", suggestions);
  }

  private boolean hasScanAmplification(long rowsExamined, long rowsSent) {
    long safeRowsSent = Math.max(1L, rowsSent);
    return rowsExamined >= 1_000L && rowsExamined > safeRowsSent * ROW_SCAN_AMPLIFICATION;
  }

  private String normalizeSql(String sqlDigest) {
    if (sqlDigest == null || sqlDigest.trim().isEmpty()) {
      return "未知 SQL";
    }
    return sqlDigest.trim().replaceAll("\\s+", " ");
  }
}
