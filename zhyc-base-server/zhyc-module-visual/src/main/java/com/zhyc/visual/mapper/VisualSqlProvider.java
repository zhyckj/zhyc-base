/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.mapper;

import java.util.Map;

/**
 * 可视化报表 SQL Provider。
 */
public class VisualSqlProvider {

  /**
   * 生成测试可读的数据集查询 SQL。
   *
   * @param tenantId 租户业务编码
   * @param status 数据集状态
   * @return 数据集查询 SQL
   */
  public String selectDatasets(String tenantId, String status) {
    return selectDatasetsSql(status);
  }

  /**
   * 生成 MyBatis 数据集查询 SQL。
   *
   * @param params 查询参数
   * @return 数据集查询 SQL
   */
  public String selectDatasetsForMapper(Map<String, Object> params) {
    return selectDatasetsSql(params.get("status"));
  }

  /**
   * 生成单个数据集查询 SQL。
   *
   * @return 单个数据集查询 SQL
   */
  public String selectDatasetByCode() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               dataset_code AS datasetCode,
               dataset_name AS datasetName,
               datasource_code AS datasourceCode,
               sql_text AS sqlText,
               dataset_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM visual_dataset
        WHERE tenant_id = #{tenantId}
          AND dataset_code = #{datasetCode}
          AND deleted = 0
        LIMIT 1
        """;
  }

  /**
   * 生成测试可读的报表查询 SQL。
   *
   * @param tenantId 租户业务编码
   * @param status 报表状态
   * @return 报表查询 SQL
   */
  public String selectReports(String tenantId, String status) {
    return selectReportsSql(status);
  }

  /**
   * 生成 MyBatis 报表查询 SQL。
   *
   * @param params 查询参数
   * @return 报表查询 SQL
   */
  public String selectReportsForMapper(Map<String, Object> params) {
    return selectReportsSql(params.get("status"));
  }

  /**
   * 生成测试可读的大屏查询 SQL。
   *
   * @param tenantId 租户业务编码
   * @param status 大屏状态
   * @return 大屏查询 SQL
   */
  public String selectScreens(String tenantId, String status) {
    return selectScreensSql(status);
  }

  /**
   * 生成 MyBatis 大屏查询 SQL。
   *
   * @param params 查询参数
   * @return 大屏查询 SQL
   */
  public String selectScreensForMapper(Map<String, Object> params) {
    return selectScreensSql(params.get("status"));
  }

  /**
   * 生成数据集保存 SQL。
   *
   * @return 数据集保存 SQL
   */
  public String upsertDataset() {
    return """
        INSERT INTO visual_dataset (
            tenant_id, dataset_code, dataset_name, datasource_code, sql_text, dataset_status
        ) VALUES (
            #{tenantId}, #{datasetCode}, #{datasetName}, #{datasourceCode}, #{sqlText}, #{status}
        )
        ON DUPLICATE KEY UPDATE
            dataset_name = VALUES(dataset_name),
            datasource_code = VALUES(datasource_code),
            sql_text = VALUES(sql_text),
            dataset_status = VALUES(dataset_status),
            updated_at = CURRENT_TIMESTAMP,
            deleted = 0
        """;
  }

  /**
   * 生成报表保存 SQL。
   *
   * @return 报表保存 SQL
   */
  public String upsertReport() {
    return """
        INSERT INTO visual_report (
            tenant_id, report_code, report_name, dataset_code, chart_type, config_json, report_status
        ) VALUES (
            #{tenantId}, #{reportCode}, #{reportName}, #{datasetCode}, #{chartType}, #{configJson}, #{status}
        )
        ON DUPLICATE KEY UPDATE
            report_name = VALUES(report_name),
            dataset_code = VALUES(dataset_code),
            chart_type = VALUES(chart_type),
            config_json = VALUES(config_json),
            report_status = VALUES(report_status),
            updated_at = CURRENT_TIMESTAMP,
            deleted = 0
        """;
  }

  /**
   * 生成大屏保存 SQL。
   *
   * @return 大屏保存 SQL
   */
  public String upsertScreen() {
    return """
        INSERT INTO visual_screen (
            tenant_id, screen_code, screen_name, layout_json, screen_status
        ) VALUES (
            #{tenantId}, #{screenCode}, #{screenName}, #{layoutJson}, #{status}
        )
        ON DUPLICATE KEY UPDATE
            screen_name = VALUES(screen_name),
            layout_json = VALUES(layout_json),
            screen_status = VALUES(screen_status),
            updated_at = CURRENT_TIMESTAMP,
            deleted = 0
        """;
  }

  /**
   * 生成大屏状态更新 SQL。
   *
   * @return 大屏状态更新 SQL
   */
  public String updateScreenStatus() {
    return """
        UPDATE visual_screen
        SET screen_status = #{status},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId}
          AND id = #{id}
          AND deleted = 0
        """;
  }

  /**
   * 生成报表状态更新 SQL。
   *
   * @return 报表状态更新 SQL
   */
  public String updateReportStatus() {
    return """
        UPDATE visual_report
        SET report_status = #{status},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId}
          AND id = #{id}
          AND deleted = 0
        """;
  }

  /**
   * 生成公开访问的已发布报表查询 SQL。
   *
   * @return 已发布报表查询 SQL
   */
  public String selectPublishedReport() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               report_code AS reportCode,
               report_name AS reportName,
               dataset_code AS datasetCode,
               chart_type AS chartType,
               config_json AS configJson,
               report_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM visual_report
        WHERE tenant_id = #{tenantId}
          AND report_code = #{reportCode}
          AND report_status = 'published'
          AND deleted = 0
        LIMIT 1
        """;
  }

  /**
   * 生成公开访问的已发布大屏查询 SQL。
   *
   * @return 已发布大屏查询 SQL
   */
  public String selectPublishedScreen() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               screen_code AS screenCode,
               screen_name AS screenName,
               layout_json AS layoutJson,
               screen_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM visual_screen
        WHERE tenant_id = #{tenantId}
          AND screen_code = #{screenCode}
          AND screen_status = 'published'
          AND deleted = 0
        LIMIT 1
        """;
  }

  private String selectDatasetsSql(Object status) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               dataset_code AS datasetCode,
               dataset_name AS datasetName,
               datasource_code AS datasourceCode,
               sql_text AS sqlText,
               dataset_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM visual_dataset
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        """);
    if (status != null && !status.toString().isBlank()) {
      sql.append("  AND dataset_status = #{status}\n");
    }
    sql.append("ORDER BY updated_at DESC, id DESC\n");
    return sql.toString();
  }

  private String selectReportsSql(Object status) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               report_code AS reportCode,
               report_name AS reportName,
               dataset_code AS datasetCode,
               chart_type AS chartType,
               config_json AS configJson,
               report_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM visual_report
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        """);
    if (status != null && !status.toString().isBlank()) {
      sql.append("  AND report_status = #{status}\n");
    }
    sql.append("ORDER BY updated_at DESC, id DESC\n");
    return sql.toString();
  }

  private String selectScreensSql(Object status) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               screen_code AS screenCode,
               screen_name AS screenName,
               layout_json AS layoutJson,
               screen_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM visual_screen
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        """);
    if (status != null && !status.toString().isBlank()) {
      sql.append("  AND screen_status = #{status}\n");
    }
    sql.append("ORDER BY updated_at DESC, id DESC\n");
    return sql.toString();
  }
}
