/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual;

import com.zhyc.visual.mapper.VisualSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 可视化报表 SQL 生成测试。
 */
class VisualSqlProviderTest {

  /**
   * 验证数据集查询 SQL 包含租户、状态和逻辑删除条件。
   */
  @Test
  void shouldBuildDatasetQueryWithTenantStatusAndDeletedFilter() {
    String sql = new VisualSqlProvider().selectDatasets("tenant_a", "enabled");

    assertTrue(sql.contains("FROM visual_dataset"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("dataset_status = #{status}"));
    assertTrue(sql.contains("deleted = 0"));
  }

  /**
   * 验证单个数据集查询 SQL 包含租户、编码、逻辑删除和单行限制。
   */
  @Test
  void shouldBuildDatasetCodeQueryWithTenantCodeAndDeletedFilter() {
    String sql = new VisualSqlProvider().selectDatasetByCode();

    assertTrue(sql.contains("FROM visual_dataset"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("dataset_code = #{datasetCode}"));
    assertTrue(sql.contains("deleted = 0"));
    assertTrue(sql.contains("LIMIT 1"));
  }

  /**
   * 验证报表查询 SQL 包含租户和状态条件。
   */
  @Test
  void shouldBuildReportQueryWithTenantAndStatusFilter() {
    String sql = new VisualSqlProvider().selectReports("tenant_a", "enabled");

    assertTrue(sql.contains("FROM visual_report"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("report_status = #{status}"));
  }

  /**
   * 验证公开报表查询 SQL 只返回指定租户、编码且已发布的报表。
   */
  @Test
  void shouldBuildPublishedReportQueryWithTenantCodeStatusAndDeletedFilter() {
    String sql = new VisualSqlProvider().selectPublishedReport();

    assertTrue(sql.contains("FROM visual_report"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("report_code = #{reportCode}"));
    assertTrue(sql.contains("report_status = 'published'"));
    assertTrue(sql.contains("deleted = 0"));
    assertTrue(sql.contains("LIMIT 1"));
  }

  /**
   * 验证看板查询 SQL 包含租户和发布状态条件。
   */
  @Test
  void shouldBuildScreenQueryWithTenantAndStatusFilter() {
    String sql = new VisualSqlProvider().selectScreens("tenant_a", "published");

    assertTrue(sql.contains("FROM visual_screen"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("screen_status = #{status}"));
  }

  /**
   * 验证公开大屏查询 SQL 只返回指定租户、编码且已发布的大屏。
   */
  @Test
  void shouldBuildPublishedScreenQueryWithTenantCodeStatusAndDeletedFilter() {
    String sql = new VisualSqlProvider().selectPublishedScreen();

    assertTrue(sql.contains("FROM visual_screen"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("screen_code = #{screenCode}"));
    assertTrue(sql.contains("screen_status = 'published'"));
    assertTrue(sql.contains("deleted = 0"));
    assertTrue(sql.contains("LIMIT 1"));
  }
}
