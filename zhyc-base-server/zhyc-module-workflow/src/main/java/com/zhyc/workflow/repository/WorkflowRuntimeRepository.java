/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.repository;

import com.zhyc.common.workflow.WorkflowTaskActionContext;
import com.zhyc.workflow.service.WorkflowCcTaskItem;
import com.zhyc.workflow.service.WorkflowProcessMonitorItem;
import com.zhyc.workflow.service.WorkflowStartedProcessItem;
import com.zhyc.workflow.service.WorkflowTaskTodoItem;
import com.zhyc.workflow.service.WorkflowTaskDetailResponse;
import java.util.List;
import java.util.Map;

/**
 * 工作流运行仓储。
 */
public interface WorkflowRuntimeRepository {

  /**
   * 创建流程实例和首个待办任务。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param processKey 流程定义 key
   * @param businessKey 业务对象唯一标识
   * @param starterUserId 流程发起人用户 ID
   * @param firstTaskId 首个任务 ID
   * @param firstTaskName 首个任务名称
   * @param firstTaskAssigneeUserId 首个任务处理人用户 ID
   */
  void startProcess(String tenantId, String processInstanceId, String processKey,
      String businessKey, Long starterUserId, String firstTaskId, String firstTaskName,
      Long firstTaskAssigneeUserId);

  /**
   * 查询待办任务。
   *
   * @param tenantId 租户业务编码
   * @param assigneeUserId 任务处理人用户 ID
   * @return 待办任务列表
   */
  List<WorkflowTaskTodoItem> findTodoTasks(String tenantId, Long assigneeUserId);

  /**
   * 查询已办任务。
   *
   * @param tenantId 租户业务编码
   * @param operatorUserId 操作用户 ID
   * @return 已办任务列表
   */
  List<WorkflowTaskTodoItem> findDoneTasks(String tenantId, Long operatorUserId);

  /**
   * 查询当前用户发起的流程实例。
   *
   * @param tenantId 租户业务编码
   * @param starterUserId 流程发起人用户 ID
   * @return 发起流程列表
   */
  List<WorkflowStartedProcessItem> findStartedProcesses(String tenantId, Long starterUserId);

  /**
   * 查询当前用户收到的抄送任务。
   *
   * @param tenantId 租户业务编码
   * @param receiverId 抄送接收人用户 ID
   * @return 抄送任务列表
   */
  List<WorkflowCcTaskItem> findCcTasks(String tenantId, Long receiverId);

  /**
   * 查询租户下全部流程实例监控列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程监控列表
   */
  List<WorkflowProcessMonitorItem> findMonitoredProcesses(String tenantId);

  /**
   * 查询当前用户可访问的任务详情。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param assigneeUserId 当前处理人用户 ID
   * @return 任务详情，不存在时返回 {@code null}
   */
  WorkflowTaskDetailResponse findTaskDetail(String tenantId, String taskId, Long assigneeUserId);

  /**
   * 处理任务并记录审批动作。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param operatorUserId 操作用户 ID
   * @param action 处理动作
   * @param comment 审批意见
   * @param variables 任务处理变量
   */
  WorkflowTaskActionContext handleTask(String tenantId, String taskId, Long operatorUserId, String action,
      String comment, Map<String, Object> variables);

  /**
   * 撤回流程实例并终止未完成任务。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param operatorUserId 操作用户 ID
   * @param reason 撤回原因
   */
  void revokeProcess(String tenantId, String processInstanceId, Long operatorUserId, String reason);
}
