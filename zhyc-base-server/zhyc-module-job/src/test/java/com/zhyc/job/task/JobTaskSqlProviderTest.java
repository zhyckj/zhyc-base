/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task;

import com.zhyc.job.task.mapper.JobTaskSqlProvider;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 在线作业任务 SQL Provider 测试。
 */
class JobTaskSqlProviderTest {

  /**
   * 验证作业任务查询 SQL 包含租户隔离、状态筛选和逻辑删除条件。
   */
  @Test
  void shouldBuildTenantScopedTaskQuerySql() {
    String sql = new JobTaskSqlProvider().selectTasks(Map.of("status", "enabled"));

    assertTrue(sql.contains("FROM job_task"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("deleted = 0"));
    assertTrue(sql.contains("job_status = #{status}"));
    assertTrue(sql.contains("ORDER BY updated_at DESC, id DESC"));
  }

  /**
   * 验证触发前作业详情查询 SQL 受租户、主键和逻辑删除条件约束。
   */
  @Test
  void shouldBuildTenantScopedTaskDetailSql() {
    String sql = new JobTaskSqlProvider().selectByTenantAndId();

    assertTrue(sql.contains("FROM job_task"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("id = #{id}"));
    assertTrue(sql.contains("deleted = 0"));
  }

  /**
   * 验证作业执行日志 SQL 包含租户隔离和任务筛选。
   */
  @Test
  void shouldBuildTenantScopedLogQuerySql() {
    String sql = new JobTaskSqlProvider().selectLogs(Map.of("jobId", 10L));

    assertTrue(sql.contains("FROM job_task_log"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("job_id = #{jobId}"));
    assertTrue(sql.contains("ORDER BY start_at DESC, id DESC"));
  }
}
