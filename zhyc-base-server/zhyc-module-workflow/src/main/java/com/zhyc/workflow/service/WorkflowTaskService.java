/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.service;

import java.util.List;

/**
 * 工作流任务服务。
 */
public interface WorkflowTaskService {

  /**
   * 查询当前用户待办任务。
   *
   * @param tenantId 租户业务编码
   * @param assigneeUserId 任务处理人用户 ID
   * @return 待办任务列表
   */
  List<WorkflowTaskTodoItem> listTodoTasks(String tenantId, Long assigneeUserId);

  /**
   * 查询当前用户已办任务。
   *
   * @param tenantId 租户业务编码
   * @param operatorUserId 当前操作用户 ID
   * @return 已办任务列表
   */
  List<WorkflowTaskTodoItem> listDoneTasks(String tenantId, Long operatorUserId);

  /**
   * 查询当前用户发起的流程实例。
   *
   * @param tenantId 租户业务编码
   * @param starterUserId 流程发起人用户 ID
   * @return 发起流程列表
   */
  List<WorkflowStartedProcessItem> listStartedProcesses(String tenantId, Long starterUserId);

  /**
   * 查询当前用户收到的抄送任务。
   *
   * @param tenantId 租户业务编码
   * @param receiverId 抄送接收人用户 ID
   * @return 抄送任务列表
   */
  List<WorkflowCcTaskItem> listCcTasks(String tenantId, Long receiverId);

  /**
   * 查询租户下流程实例监控列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程监控列表
   */
  List<WorkflowProcessMonitorItem> listMonitoredProcesses(String tenantId);

  /**
   * 查询当前用户可访问的任务详情。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param assigneeUserId 当前处理人用户 ID
   * @return 任务详情
   */
  WorkflowTaskDetailResponse getTaskDetail(String tenantId, String taskId, Long assigneeUserId);

  /**
   * 审批通过任务。
   *
   * @param command 任务处理命令
   */
  void approve(WorkflowTaskHandleCommand command);

  /**
   * 驳回任务。
   *
   * @param command 任务处理命令
   */
  void reject(WorkflowTaskHandleCommand command);

  /**
   * 撤回流程实例。
   *
   * @param command 流程撤回命令
   */
  void revoke(WorkflowProcessRevokeCommand command);
}
