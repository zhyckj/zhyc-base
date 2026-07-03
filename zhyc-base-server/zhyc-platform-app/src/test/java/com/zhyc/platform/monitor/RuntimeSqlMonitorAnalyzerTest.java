/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SQL 运行监控分析器测试。
 */
class RuntimeSqlMonitorAnalyzerTest {

  /**
   * 验证扫描行数远大于返回行数时给出索引优化建议。
   */
  @Test
  void shouldSuggestIndexOptimizationForHighRowsExamined() {
    RuntimeSqlMonitorAnalyzer analyzer = new RuntimeSqlMonitorAnalyzer();

    RuntimeSqlMonitorRecord record = analyzer.analyze("mainDataSource",
        "SELECT * FROM sys_user WHERE login_account = ? ORDER BY created_at DESC",
        128L, 860L, 2600L, 500_000L, 20L, "2026-06-29 15:00:00");

    assertEquals("SLOW", record.severity());
    assertTrue(record.suggestion().contains("补充匹配 WHERE 条件和排序字段的联合索引"));
    assertTrue(record.suggestion().contains("避免 SELECT *"));
  }

  /**
   * 验证平均耗时超过 1 秒时标记为严重慢 SQL。
   */
  @Test
  void shouldMarkCriticalWhenAverageCostTooHigh() {
    RuntimeSqlMonitorAnalyzer analyzer = new RuntimeSqlMonitorAnalyzer();

    RuntimeSqlMonitorRecord record = analyzer.analyze("mainDataSource",
        "SELECT id FROM pur_request WHERE tenant_id = ?",
        20L, 1200L, 5000L, 2000L, 1800L, "2026-06-29 15:00:00");

    assertEquals("CRITICAL", record.severity());
    assertTrue(record.suggestion().contains("优先执行 EXPLAIN"));
  }
}
