/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.service;

import com.zhyc.common.workflow.WorkflowTaskActionContext;
import com.zhyc.common.workflow.WorkflowTaskActionHandler;
import com.zhyc.workflow.repository.WorkflowRuntimeRepository;
import com.zhyc.workflow.support.WorkflowServiceValidation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认工作流任务服务实现。
 */
@Service
public class DefaultWorkflowTaskService implements WorkflowTaskService {

  /** 审批通过动作。 */
  private static final String ACTION_APPROVE = "APPROVE";
  /** 驳回动作。 */
  private static final String ACTION_REJECT = "REJECT";
  /** 撤回动作。 */
  private static final String ACTION_REVOKE = "REVOKE";

  /** 工作流运行仓储。 */
  private final WorkflowRuntimeRepository workflowRuntimeRepository;
  /** 工作流任务动作处理器列表。 */
  private final List<WorkflowTaskActionHandler> actionHandlers;

  /**
   * 创建默认工作流任务服务。
   *
   * @param workflowRuntimeRepository 工作流运行仓储
   */
  public DefaultWorkflowTaskService(WorkflowRuntimeRepository workflowRuntimeRepository) {
    this(workflowRuntimeRepository, List.of());
  }

  /**
   * 创建默认工作流任务服务。
   *
   * @param workflowRuntimeRepository 工作流运行仓储
   * @param actionHandlers 工作流任务动作处理器列表
   */
  @Autowired
  public DefaultWorkflowTaskService(WorkflowRuntimeRepository workflowRuntimeRepository,
      List<WorkflowTaskActionHandler> actionHandlers) {
    this.workflowRuntimeRepository = Objects.requireNonNull(workflowRuntimeRepository,
        "工作流运行仓储不能为空");
    this.actionHandlers = List.copyOf(Objects.requireNonNull(actionHandlers,
        "工作流任务动作处理器列表不能为空"));
  }

  /**
   * 查询当前处理人的待办任务。
   *
   * @param tenantId 租户业务编码
   * @param assigneeUserId 当前处理人用户 ID
   * @return 待办任务列表
   */
  @Override
  public List<WorkflowTaskTodoItem> listTodoTasks(String tenantId, Long assigneeUserId) {
    return workflowRuntimeRepository.findTodoTasks(requireText(tenantId, "租户业务编码不能为空"),
        WorkflowServiceValidation.requireLong(assigneeUserId, "处理人用户 ID 不能为空"));
  }

  /**
   * 查询当前操作人的已办任务。
   *
   * @param tenantId 租户业务编码
   * @param operatorUserId 当前操作人用户 ID
   * @return 已办任务列表
   */
  @Override
  public List<WorkflowTaskTodoItem> listDoneTasks(String tenantId, Long operatorUserId) {
    return workflowRuntimeRepository.findDoneTasks(requireText(tenantId, "租户业务编码不能为空"),
        WorkflowServiceValidation.requireLong(operatorUserId, "操作用户 ID 不能为空"));
  }

  /**
   * 查询当前发起人的流程实例。
   *
   * @param tenantId 租户业务编码
   * @param starterUserId 流程发起人用户 ID
   * @return 发起流程列表
   */
  @Override
  public List<WorkflowStartedProcessItem> listStartedProcesses(String tenantId,
      Long starterUserId) {
    return workflowRuntimeRepository.findStartedProcesses(requireText(tenantId, "租户业务编码不能为空"),
        WorkflowServiceValidation.requireLong(starterUserId, "发起人用户 ID 不能为空"));
  }

  /**
   * 查询当前接收人的抄送任务。
   *
   * @param tenantId 租户业务编码
   * @param receiverId 抄送接收人用户 ID
   * @return 抄送任务列表
   */
  @Override
  public List<WorkflowCcTaskItem> listCcTasks(String tenantId, Long receiverId) {
    return workflowRuntimeRepository.findCcTasks(requireText(tenantId, "租户业务编码不能为空"),
        WorkflowServiceValidation.requireLong(receiverId, "抄送接收人用户 ID 不能为空"));
  }

  /**
   * 查询租户下流程实例监控列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程监控列表
   */
  @Override
  public List<WorkflowProcessMonitorItem> listMonitoredProcesses(String tenantId) {
    return workflowRuntimeRepository.findMonitoredProcesses(
        requireText(tenantId, "租户业务编码不能为空"));
  }

  /**
   * 查询任务详情和审批记录。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param assigneeUserId 当前处理人用户 ID
   * @return 任务详情
   */
  @Override
  public WorkflowTaskDetailResponse getTaskDetail(String tenantId, String taskId,
      Long assigneeUserId) {
    WorkflowTaskDetailResponse detail = workflowRuntimeRepository.findTaskDetail(
        requireText(tenantId, "租户业务编码不能为空"),
        requireText(taskId, "任务 ID 不能为空"),
        WorkflowServiceValidation.requireLong(assigneeUserId, "处理人用户 ID 不能为空"));
    if (detail == null) {
      throw WorkflowServiceValidation.businessFailure("工作流任务不存在或无权访问");
    }
    return detail;
  }

  /**
   * 审批通过任务。
   *
   * @param command 任务处理命令
   */
  @Override
  @Transactional
  public void approve(WorkflowTaskHandleCommand command) {
    handle(command, ACTION_APPROVE);
  }

  /**
   * 驳回任务。
   *
   * @param command 任务处理命令
   */
  @Override
  @Transactional
  public void reject(WorkflowTaskHandleCommand command) {
    handle(command, ACTION_REJECT);
  }

  /**
   * 撤回流程实例。
   *
   * @param command 流程撤回命令
   */
  @Override
  @Transactional
  public void revoke(WorkflowProcessRevokeCommand command) {
    WorkflowProcessRevokeCommand requiredCommand = WorkflowServiceValidation.requireObject(command,
        "工作流流程撤回命令不能为空");
    workflowRuntimeRepository.revokeProcess(
        requireText(requiredCommand.getTenantId(), "租户业务编码不能为空"),
        requireText(requiredCommand.getProcessInstanceId(), "流程实例 ID 不能为空"),
        WorkflowServiceValidation.requireLong(requiredCommand.getOperatorUserId(), "操作用户 ID 不能为空"),
        requireText(requiredCommand.getReason(), "撤回原因不能为空"));
  }

  /**
   * 处理工作流任务。
   *
   * @param command 任务处理命令
   * @param action 处理动作
   */
  private void handle(WorkflowTaskHandleCommand command, String action) {
    WorkflowTaskHandleCommand requiredCommand = WorkflowServiceValidation.requireObject(command,
        "工作流任务处理命令不能为空");
    WorkflowTaskActionContext context = workflowRuntimeRepository.handleTask(
        requireText(requiredCommand.getTenantId(), "租户业务编码不能为空"),
        requireText(requiredCommand.getTaskId(), "任务 ID 不能为空"),
        WorkflowServiceValidation.requireLong(requiredCommand.getOperatorUserId(), "操作用户 ID 不能为空"),
        action,
        requiredCommand.getComment(),
        normalizeVariables(requiredCommand.getVariables()));
    if (context != null) {
      for (WorkflowTaskActionHandler actionHandler : actionHandlers) {
        actionHandler.handle(context);
      }
    }
  }

  /**
   * 规范化任务变量。
   *
   * @param variables 原始变量
   * @return 非空变量
   */
  private Map<String, Object> normalizeVariables(Map<String, Object> variables) {
    return variables == null ? Collections.emptyMap() : variables;
  }

  /**
   * 校验文本不能为空并去除首尾空白。
   *
   * @param value 原始文本
   * @param message 为空时的异常消息
   * @return 清理后的文本
   */
  private String requireText(String value, String message) {
    if (value == null) {
      throw WorkflowServiceValidation.businessFailure(message);
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw WorkflowServiceValidation.businessFailure(message);
    }
    return trimmed;
  }
}
