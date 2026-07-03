/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.mapper;

import com.zhyc.workflow.constant.WorkflowRuntimeStatus;

/**
 * 工作流运行 SQL Provider。
 */
public class WorkflowSqlProvider {

  /**
   * 生成流程实例写入 SQL。
   *
   * @return 流程实例写入 SQL
   */
  public String insertProcessInstance() {
    return """
        INSERT INTO wf_process_instance (
            tenant_id, process_instance_id, process_key, business_key, starter_user_id, status
        ) VALUES (
            #{tenantId}, #{processInstanceId}, #{processKey}, #{businessKey}, #{starterUserId},
            %s
        )
        """.formatted(WorkflowRuntimeStatus.RUNNING.sqlLiteral());
  }

  /**
   * 生成任务写入 SQL。
   *
   * @return 任务写入 SQL
   */
  public String insertTask() {
    return """
        INSERT INTO wf_task (
            tenant_id, task_id, process_instance_id, task_name, business_key, assignee_user_id, status
        ) VALUES (
            #{tenantId}, #{taskId}, #{processInstanceId}, #{taskName}, #{businessKey},
            #{assigneeUserId}, %s
        )
        """.formatted(WorkflowRuntimeStatus.TODO.sqlLiteral());
  }

  /**
   * 生成待办任务查询 SQL。
   *
   * @return 待办任务查询 SQL
   */
  public String selectTodoTasks() {
    return """
        SELECT tenant_id AS tenantId,
               task_id AS taskId,
               process_instance_id AS processInstanceId,
               task_name AS taskName,
               business_key AS businessKey,
               assignee_user_id AS assigneeUserId,
               status,
               created_at AS createdAt
        FROM wf_task
        WHERE tenant_id = #{tenantId}
          AND assignee_user_id = #{assigneeUserId}
          AND status = %s
          AND deleted = 0
        ORDER BY created_at DESC, id DESC
        """.formatted(WorkflowRuntimeStatus.TODO.sqlLiteral());
  }

  /**
   * 生成已办任务查询 SQL。
   *
   * @return 已办任务查询 SQL
   */
  public String selectDoneTasks() {
    return """
        SELECT t.tenant_id AS tenantId,
               t.task_id AS taskId,
               t.process_instance_id AS processInstanceId,
               t.task_name AS taskName,
               t.business_key AS businessKey,
               t.assignee_user_id AS assigneeUserId,
               t.status,
               t.completed_at AS createdAt
        FROM wf_task t
        WHERE t.tenant_id = #{tenantId}
          AND t.assignee_user_id = #{operatorUserId}
          AND t.status <> %s
          AND t.deleted = 0
        ORDER BY t.completed_at DESC, t.id DESC
        """.formatted(WorkflowRuntimeStatus.TODO.sqlLiteral());
  }

  /**
   * 生成当前用户发起流程查询 SQL。
   *
   * @return 发起流程查询 SQL
   */
  public String selectStartedProcesses() {
    return """
        SELECT tenant_id AS tenantId,
               process_instance_id AS processInstanceId,
               process_key AS processKey,
               business_key AS businessKey,
               starter_user_id AS starterUserId,
               status,
               started_at AS startedAt
        FROM wf_process_instance
        WHERE tenant_id = #{tenantId}
          AND starter_user_id = #{starterUserId}
          AND deleted = 0
        ORDER BY started_at DESC, id DESC
        """;
  }

  /**
   * 生成当前用户抄送任务查询 SQL。
   *
   * @return 抄送任务查询 SQL
   */
  public String selectCcTasks() {
    return """
        SELECT c.tenant_id AS tenantId,
               c.id AS ccRecordId,
               c.process_instance_id AS processInstanceId,
               p.process_key AS processKey,
               p.business_key AS businessKey,
               c.receiver_id AS receiverId,
               c.read_flag AS readFlag,
               c.created_at AS createdAt
        FROM wf_cc_record c
        LEFT JOIN wf_process_instance p
          ON p.tenant_id = c.tenant_id
         AND p.process_instance_id = c.process_instance_id
         AND p.deleted = 0
        WHERE c.tenant_id = #{tenantId}
          AND c.receiver_id = #{receiverId}
          AND c.deleted = 0
        ORDER BY c.created_at DESC, c.id DESC
        """;
  }

  /**
   * 生成流程监控查询 SQL。
   *
   * @return 流程监控查询 SQL
   */
  public String selectMonitoredProcesses() {
    return """
        SELECT tenant_id AS tenantId,
               process_instance_id AS processInstanceId,
               process_key AS processKey,
               business_key AS businessKey,
               starter_user_id AS starterUserId,
               status,
               started_at AS startedAt,
               ended_at AS endedAt
        FROM wf_process_instance
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        ORDER BY started_at DESC, id DESC
        """;
  }

  /**
   * 生成任务详情查询 SQL。
   *
   * @return 任务详情查询 SQL
   */
  public String selectTaskDetail() {
    return """
        SELECT t.tenant_id AS tenantId,
               t.task_id AS taskId,
               t.process_instance_id AS processInstanceId,
               p.process_key AS processKey,
               t.task_name AS taskName,
               t.business_key AS businessKey,
               t.assignee_user_id AS assigneeUserId,
               t.status,
               t.created_at AS createdAt
        FROM wf_task t
        LEFT JOIN wf_process_instance p
          ON p.tenant_id = t.tenant_id
         AND p.process_instance_id = t.process_instance_id
         AND p.deleted = 0
        WHERE t.tenant_id = #{tenantId}
          AND t.task_id = #{taskId}
          AND t.assignee_user_id = #{assigneeUserId}
          AND t.deleted = 0
        """;
  }

  /**
   * 生成审批记录查询 SQL。
   *
   * @return 审批记录查询 SQL
   */
  public String selectApprovalRecords() {
    return """
        SELECT task_id AS taskId,
               operator_user_id AS operatorUserId,
               action,
               approval_comment AS approvalComment,
               operated_at AS operatedAt
        FROM wf_approval_record
        WHERE tenant_id = #{tenantId}
          AND process_instance_id = #{processInstanceId}
        ORDER BY operated_at ASC, id ASC
        """;
  }

  /**
   * 生成任务动作上下文查询 SQL。
   *
   * @return 任务动作上下文查询 SQL
   */
  public String selectTaskActionContext() {
    return """
        SELECT t.tenant_id AS tenantId,
               t.task_id AS taskId,
               t.process_instance_id AS processInstanceId,
               p.process_key AS processKey,
               t.business_key AS businessKey,
               #{operatorUserId} AS operatorUserId,
               #{action} AS action,
               #{comment} AS comment
        FROM wf_task t
        LEFT JOIN wf_process_instance p
          ON p.tenant_id = t.tenant_id
         AND p.process_instance_id = t.process_instance_id
         AND p.deleted = 0
        WHERE t.tenant_id = #{tenantId}
          AND t.task_id = #{taskId}
          AND t.assignee_user_id = #{operatorUserId}
          AND t.status = %s
          AND t.deleted = 0
        """.formatted(WorkflowRuntimeStatus.TODO.sqlLiteral());
  }

  /**
   * 生成任务处理状态更新 SQL。
   *
   * @return 任务处理状态更新 SQL
   */
  public String updateTaskHandled() {
    return """
        UPDATE wf_task
        SET status = #{status},
            completed_at = #{completedAt}
        WHERE tenant_id = #{tenantId}
          AND task_id = #{taskId}
          AND assignee_user_id = #{operatorUserId}
          AND status = %s
          AND deleted = 0
        """.formatted(WorkflowRuntimeStatus.TODO.sqlLiteral());
  }

  /**
   * 生成流程实例撤回 SQL。
   *
   * @return 流程实例撤回 SQL
   */
  public String updateProcessRevoked() {
    return """
        UPDATE wf_process_instance
        SET status = %s,
            ended_at = #{endedAt}
        WHERE tenant_id = #{tenantId}
          AND process_instance_id = #{processInstanceId}
          AND status = %s
          AND deleted = 0
        """.formatted(WorkflowRuntimeStatus.REVOKED.sqlLiteral(),
        WorkflowRuntimeStatus.RUNNING.sqlLiteral());
  }

  /**
   * 生成流程未办任务终止 SQL。
   *
   * @return 流程未办任务终止 SQL
   */
  public String updateProcessTodoTasksHandled() {
    return """
        UPDATE wf_task
        SET status = #{status},
            completed_at = #{completedAt}
        WHERE tenant_id = #{tenantId}
          AND process_instance_id = #{processInstanceId}
          AND status = %s
          AND deleted = 0
        """.formatted(WorkflowRuntimeStatus.TODO.sqlLiteral());
  }

  /**
   * 生成审批记录写入 SQL。
   *
   * @return 审批记录写入 SQL
   */
  public String insertApprovalRecord() {
    return """
        INSERT INTO wf_approval_record (
            tenant_id, task_id, process_instance_id, operator_user_id, action, approval_comment
        ) VALUES (
            #{tenantId},
            #{taskId},
            (SELECT process_instance_id FROM wf_task
             WHERE tenant_id = #{tenantId} AND task_id = #{taskId} LIMIT 1),
            #{operatorUserId},
            #{action},
            #{comment}
        )
        """;
  }

  /**
   * 生成流程级审批记录写入 SQL。
   *
   * @return 流程级审批记录写入 SQL
   */
  public String insertProcessApprovalRecord() {
    return """
        INSERT INTO wf_approval_record (
            tenant_id, task_id, process_instance_id, operator_user_id, action, approval_comment
        ) VALUES (
            #{tenantId},
            #{processInstanceId},
            #{processInstanceId},
            #{operatorUserId},
            #{action},
            #{comment}
        )
        """;
  }
}
