/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.mapper;

import com.zhyc.search.repository.SearchRuntimeQuery;
import java.util.Map;

/**
 * 全文检索 SQL Provider。
 */
public class SearchSqlProvider {

  /**
   * 生成测试可读的索引配置查询 SQL。
   *
   * @param tenantId 租户业务编码
   * @param status 索引状态
   * @return 索引配置查询 SQL
   */
  public String selectIndexConfigs(String tenantId, String status) {
    return selectIndexConfigsSql(status);
  }

  /**
   * 生成 MyBatis 索引配置查询 SQL。
   *
   * @param params 查询参数
   * @return 索引配置查询 SQL
   */
  public String selectIndexConfigsForMapper(Map<String, Object> params) {
    return selectIndexConfigsSql(params.get("status"));
  }

  /**
   * 生成启用索引配置查询 SQL。
   *
   * @return 启用索引配置查询 SQL
   */
  public String selectEnabledIndexConfig() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               index_code AS indexCode,
               index_name AS indexName,
               source_table AS sourceTable,
               search_fields AS searchFields,
               filter_fields AS filterFields,
               index_status AS status,
               remark,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM search_index_config
        WHERE tenant_id = #{tenantId}
          AND index_code = #{indexCode}
          AND index_status = 'enabled'
          AND deleted = 0
        LIMIT 1
        """;
  }

  /**
   * 生成测试可读的重建任务查询 SQL。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 重建任务查询 SQL
   */
  public String selectRebuildTasks(String tenantId, String indexCode) {
    return selectRebuildTasksSql(indexCode);
  }

  /**
   * 生成 MyBatis 重建任务查询 SQL。
   *
   * @param params 查询参数
   * @return 重建任务查询 SQL
   */
  public String selectRebuildTasksForMapper(Map<String, Object> params) {
    return selectRebuildTasksSql(params.get("indexCode"));
  }

  /**
   * 生成测试可读的查询日志 SQL。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 查询日志 SQL
   */
  public String selectQueryLogs(String tenantId, String indexCode) {
    return selectQueryLogsSql(indexCode);
  }

  /**
   * 生成 MyBatis 查询日志 SQL。
   *
   * @param params 查询参数
   * @return 查询日志 SQL
   */
  public String selectQueryLogsForMapper(Map<String, Object> params) {
    return selectQueryLogsSql(params.get("indexCode"));
  }

  /**
   * 生成索引配置保存 SQL。
   *
   * @return 索引配置保存 SQL
   */
  public String upsertIndexConfig() {
    return """
        INSERT INTO search_index_config (
            tenant_id, index_code, index_name, source_table, search_fields,
            filter_fields, index_status, remark
        ) VALUES (
            #{tenantId}, #{indexCode}, #{indexName}, #{sourceTable}, #{searchFields},
            #{filterFields}, #{status}, #{remark}
        )
        ON DUPLICATE KEY UPDATE
            index_name = VALUES(index_name),
            source_table = VALUES(source_table),
            search_fields = VALUES(search_fields),
            filter_fields = VALUES(filter_fields),
            index_status = VALUES(index_status),
            remark = VALUES(remark),
            updated_at = CURRENT_TIMESTAMP,
            deleted = 0
        """;
  }

  /**
   * 生成重建任务新增 SQL。
   *
   * @return 重建任务新增 SQL
   */
  public String insertRebuildTask() {
    return """
        INSERT INTO search_rebuild_task (
            tenant_id, index_code, task_status, trigger_type
        ) VALUES (
            #{tenantId}, #{indexCode}, #{taskStatus}, #{triggerType}
        )
        """;
  }

  /**
   * 生成查询日志新增 SQL。
   *
   * @return 查询日志新增 SQL
   */
  public String insertQueryLog() {
    return """
        INSERT INTO search_query_log (
            tenant_id, index_code, keyword, result_count, cost_ms, query_status
        ) VALUES (
            #{tenantId}, #{indexCode}, #{keyword}, #{resultCount}, #{costMs}, #{queryStatus}
        )
        """;
  }

  /**
   * 生成运行时数据库检索 SQL。
   *
   * @param query 运行时查询参数
   * @return 运行时检索 SQL
   */
  public String selectSearchItems(SearchRuntimeQuery query) {
    return """
        SELECT ${selectExpression} AS item
        FROM ${sourceTable}
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
          AND (${whereExpression})
        LIMIT #{limit}
        """;
  }

  private String selectIndexConfigsSql(Object status) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               index_code AS indexCode,
               index_name AS indexName,
               source_table AS sourceTable,
               search_fields AS searchFields,
               filter_fields AS filterFields,
               index_status AS status,
               remark,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM search_index_config
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        """);
    if (status != null && !status.toString().isBlank()) {
      sql.append("  AND index_status = #{status}\n");
    }
    sql.append("ORDER BY index_code ASC\n");
    return sql.toString();
  }

  private String selectRebuildTasksSql(Object indexCode) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               index_code AS indexCode,
               task_status AS taskStatus,
               trigger_type AS triggerType,
               started_at AS startedAt,
               finished_at AS finishedAt,
               error_message AS errorMessage,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM search_rebuild_task
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        """);
    if (indexCode != null && !indexCode.toString().isBlank()) {
      sql.append("  AND index_code = #{indexCode}\n");
    }
    sql.append("ORDER BY id DESC\n");
    return sql.toString();
  }

  private String selectQueryLogsSql(Object indexCode) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               index_code AS indexCode,
               keyword,
               result_count AS resultCount,
               cost_ms AS costMs,
               query_status AS queryStatus,
               created_at AS createdAt
        FROM search_query_log
        WHERE tenant_id = #{tenantId}
        """);
    if (indexCode != null && !indexCode.toString().isBlank()) {
      sql.append("  AND index_code = #{indexCode}\n");
    }
    sql.append("ORDER BY id DESC\n");
    return sql.toString();
  }
}
