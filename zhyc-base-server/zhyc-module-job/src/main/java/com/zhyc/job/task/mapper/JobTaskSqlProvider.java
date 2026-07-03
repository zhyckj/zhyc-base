/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.mapper;

import java.util.Map;

/**
 * 在线作业任务 SQL Provider。
 */
public class JobTaskSqlProvider {

  /**
   * 生成作业任务查询 SQL。
   *
   * @param params 查询参数
   * @return 作业任务查询 SQL
   */
  public String selectTasks(Map<String, Object> params) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               job_code AS jobCode,
               job_name AS jobName,
               cron_expression AS cronExpression,
               handler_name AS handlerName,
               job_description AS jobDescription,
               job_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM job_task
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        """);
    Object status = params.get("status");
    if (status != null && !status.toString().isBlank()) {
      sql.append("  AND job_status = #{status}\n");
    }
    sql.append("ORDER BY updated_at DESC, id DESC\n");
    return sql.toString();
  }

  /**
   * 生成按租户和主键查询作业任务 SQL。
   *
   * @return 作业任务详情查询 SQL
   */
  public String selectByTenantAndId() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               job_code AS jobCode,
               job_name AS jobName,
               cron_expression AS cronExpression,
               handler_name AS handlerName,
               job_description AS jobDescription,
               job_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM job_task
        WHERE tenant_id = #{tenantId}
          AND id = #{id}
          AND deleted = 0
        """;
  }

  /**
   * 生成作业任务保存 SQL。
   *
   * @return 作业任务保存 SQL
   */
  public String upsertTask() {
    return """
        INSERT INTO job_task (
            tenant_id, job_code, job_name, cron_expression, handler_name,
            job_description, job_status
        ) VALUES (
            #{tenantId}, #{jobCode}, #{jobName}, #{cronExpression}, #{handlerName},
            #{jobDescription}, #{status}
        )
        ON DUPLICATE KEY UPDATE
            job_name = VALUES(job_name),
            cron_expression = VALUES(cron_expression),
            handler_name = VALUES(handler_name),
            job_description = VALUES(job_description),
            job_status = VALUES(job_status),
            updated_at = CURRENT_TIMESTAMP,
            deleted = 0
        """;
  }

  /**
   * 生成作业任务状态更新 SQL。
   *
   * @return 作业任务状态更新 SQL
   */
  public String updateStatus() {
    return """
        UPDATE job_task
        SET job_status = #{status},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId}
          AND id = #{id}
          AND deleted = 0
        """;
  }

  /**
   * 生成作业执行日志写入 SQL。
   *
   * @return 作业执行日志写入 SQL
   */
  public String insertLog() {
    return """
        INSERT INTO job_task_log (
            tenant_id, job_id, trigger_type, start_at, end_at, result, error_message, operator_id
        ) VALUES (
            #{tenantId}, #{jobId}, #{triggerType}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
            #{result}, #{errorMessage}, #{operatorId}
        )
        """;
  }

  /**
   * 生成作业执行日志查询 SQL。
   *
   * @param params 查询参数
   * @return 作业执行日志查询 SQL
   */
  public String selectLogs(Map<String, Object> params) {
    return """
        SELECT id,
               tenant_id AS tenantId,
               job_id AS jobId,
               trigger_type AS triggerType,
               start_at AS startAt,
               end_at AS endAt,
               result,
               error_message AS errorMessage,
               operator_id AS operatorId
        FROM job_task_log
        WHERE tenant_id = #{tenantId}
          AND job_id = #{jobId}
        ORDER BY start_at DESC, id DESC
        """;
  }
}
