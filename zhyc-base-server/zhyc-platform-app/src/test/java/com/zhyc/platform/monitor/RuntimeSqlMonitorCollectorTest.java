/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * SQL 运行监控采集器测试。
 */
class RuntimeSqlMonitorCollectorTest {

  /**
   * 验证同一 SQL 摘要会聚合执行次数和耗时指标。
   */
  @Test
  void shouldAggregateSameSqlDigest() {
    RuntimeSqlMonitorCollector collector = new RuntimeSqlMonitorCollector();

    collector.record("dataSource", " SELECT  * FROM sys_user WHERE id = ? ", 20L, 1L);
    collector.record("dataSource", "SELECT * FROM sys_user WHERE id = ?", 40L, 2L);

    List<RuntimeSqlMonitorRecord> records = collector.listRecords(1, 10);

    assertEquals(1, records.size());
    RuntimeSqlMonitorRecord record = records.get(0);
    assertEquals("dataSource", record.sourceCode());
    assertEquals("SELECT * FROM sys_user WHERE id = ?", record.sqlDigest());
    assertEquals(2L, record.executeCount());
    assertEquals(30L, record.avgCostMs());
    assertEquals(40L, record.maxCostMs());
    assertEquals(3L, record.rowsSent());
    assertTrue(record.lastSeen().contains("T"));
  }

  /**
   * 验证低于阈值的 SQL 记录不会进入列表。
   */
  @Test
  void shouldFilterByThreshold() {
    RuntimeSqlMonitorCollector collector = new RuntimeSqlMonitorCollector();

    collector.record("dataSource", "SELECT id FROM sys_user", 5L, 1L);

    assertTrue(collector.listRecords(10, 10).isEmpty());
  }
}
