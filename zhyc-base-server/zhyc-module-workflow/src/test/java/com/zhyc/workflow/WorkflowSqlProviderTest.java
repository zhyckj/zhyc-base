/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhyc.workflow.mapper.WorkflowSqlProvider;
import org.junit.jupiter.api.Test;

/**
 * 工作流运行 SQL Provider 测试。
 */
class WorkflowSqlProviderTest {

  /**
   * 验证审批动作上下文查询必须同时匹配租户、任务和当前处理人。
   */
  @Test
  void shouldSelectTaskActionContextByCurrentAssignee() {
    String sql = new WorkflowSqlProvider().selectTaskActionContext();

    assertTrue(sql.contains("t.tenant_id = #{tenantId}"));
    assertTrue(sql.contains("t.task_id = #{taskId}"));
    assertTrue(sql.contains("t.assignee_user_id = #{operatorUserId}"));
    assertTrue(sql.contains("t.status = 'TODO'"));
    assertTrue(sql.contains("t.deleted = 0"));
  }

  /**
   * 验证审批任务状态更新必须同时匹配租户、任务、当前处理人和待办状态。
   */
  @Test
  void shouldUpdateTaskHandledByCurrentAssignee() {
    String sql = new WorkflowSqlProvider().updateTaskHandled();

    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("task_id = #{taskId}"));
    assertTrue(sql.contains("assignee_user_id = #{operatorUserId}"));
    assertTrue(sql.contains("status = 'TODO'"));
    assertTrue(sql.contains("deleted = 0"));
  }

  /**
   * 验证我发起的流程查询必须同时匹配租户和发起人。
   */
  @Test
  void shouldSelectStartedProcessesByTenantAndStarter() {
    String sql = new WorkflowSqlProvider().selectStartedProcesses();

    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("starter_user_id = #{starterUserId}"));
    assertTrue(sql.contains("deleted = 0"));
  }

  /**
   * 验证抄送任务查询必须同时匹配租户和抄送接收人。
   */
  @Test
  void shouldSelectCcTasksByTenantAndReceiver() {
    String sql = new WorkflowSqlProvider().selectCcTasks();

    assertTrue(sql.contains("c.tenant_id = #{tenantId}"));
    assertTrue(sql.contains("c.receiver_id = #{receiverId}"));
    assertTrue(sql.contains("c.deleted = 0"));
  }
}
