/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.repository;

import com.zhyc.common.workflow.WorkflowTaskActionContext;
import com.zhyc.workflow.constant.WorkflowRuntimeStatus;
import com.zhyc.workflow.mapper.WorkflowMapper;
import com.zhyc.workflow.service.WorkflowCcTaskItem;
import com.zhyc.workflow.service.WorkflowProcessMonitorItem;
import com.zhyc.workflow.service.WorkflowStartedProcessItem;
import com.zhyc.workflow.service.WorkflowTaskDetailResponse;
import com.zhyc.workflow.service.WorkflowTaskTodoItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * MyBatis 工作流运行仓储实现。
 */
@Repository
public class MyBatisWorkflowRuntimeRepository implements WorkflowRuntimeRepository {

  /** 审批通过动作。 */
  private static final String ACTION_APPROVE = "APPROVE";

  /** 工作流 Mapper。 */
  private final WorkflowMapper workflowMapper;

  /**
   * 创建 MyBatis 工作流运行仓储。
   *
   * @param workflowMapper 工作流 Mapper
   */
  public MyBatisWorkflowRuntimeRepository(WorkflowMapper workflowMapper) {
    this.workflowMapper = Objects.requireNonNull(workflowMapper, "工作流 Mapper 不能为空");
  }

  /**
   * 写入流程实例和首个待办任务。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param processKey 流程定义 key
   * @param businessKey 业务对象唯一标识
   * @param starterUserId 发起人用户 ID
   * @param firstTaskId 首个任务 ID
   * @param firstTaskName 首个任务名称
   * @param firstTaskAssigneeUserId 首个任务处理人用户 ID
   */
  @Override
  public void startProcess(String tenantId, String processInstanceId, String processKey,
      String businessKey, Long starterUserId, String firstTaskId, String firstTaskName,
      Long firstTaskAssigneeUserId) {
    workflowMapper.insertProcessInstance(tenantId, processInstanceId, processKey, businessKey,
        starterUserId);
    if (firstTaskAssigneeUserId != null) {
      workflowMapper.insertTask(tenantId, firstTaskId, processInstanceId, firstTaskName,
          businessKey, firstTaskAssigneeUserId);
    }
  }

  /**
   * 查询处理人的待办任务。
   *
   * @param tenantId 租户业务编码
   * @param assigneeUserId 处理人用户 ID
   * @return 待办任务列表
   */
  @Override
  public List<WorkflowTaskTodoItem> findTodoTasks(String tenantId, Long assigneeUserId) {
    return workflowMapper.selectTodoTasks(tenantId, assigneeUserId);
  }

  /**
   * 查询操作人的已办任务。
   *
   * @param tenantId 租户业务编码
   * @param operatorUserId 操作人用户 ID
   * @return 已办任务列表
   */
  @Override
  public List<WorkflowTaskTodoItem> findDoneTasks(String tenantId, Long operatorUserId) {
    return workflowMapper.selectDoneTasks(tenantId, operatorUserId);
  }

  /**
   * 查询发起人的流程实例。
   *
   * @param tenantId 租户业务编码
   * @param starterUserId 发起人用户 ID
   * @return 发起流程列表
   */
  @Override
  public List<WorkflowStartedProcessItem> findStartedProcesses(String tenantId,
      Long starterUserId) {
    return workflowMapper.selectStartedProcesses(tenantId, starterUserId);
  }

  /**
   * 查询接收人的抄送任务。
   *
   * @param tenantId 租户业务编码
   * @param receiverId 抄送接收人用户 ID
   * @return 抄送任务列表
   */
  @Override
  public List<WorkflowCcTaskItem> findCcTasks(String tenantId, Long receiverId) {
    return workflowMapper.selectCcTasks(tenantId, receiverId);
  }

  /**
   * 查询租户下全部流程实例监控列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程监控列表
   */
  @Override
  public List<WorkflowProcessMonitorItem> findMonitoredProcesses(String tenantId) {
    return workflowMapper.selectMonitoredProcesses(tenantId);
  }

  /**
   * 查询任务详情及审批记录。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param assigneeUserId 当前处理人用户 ID
   * @return 任务详情，未命中时为空
   */
  @Override
  public WorkflowTaskDetailResponse findTaskDetail(String tenantId, String taskId,
      Long assigneeUserId) {
    WorkflowTaskDetailResponse detail = workflowMapper.selectTaskDetail(tenantId, taskId,
        assigneeUserId);
    if (detail == null) {
      return null;
    }
    return new WorkflowTaskDetailResponse(detail.getTenantId(), detail.getTaskId(),
        detail.getProcessInstanceId(), detail.getProcessKey(), detail.getTaskName(),
        detail.getBusinessKey(), detail.getAssigneeUserId(), detail.getStatus(),
        detail.getCreatedAt(), workflowMapper.selectApprovalRecords(tenantId,
        detail.getProcessInstanceId()));
  }

  /**
   * 处理审批任务并写入审批记录。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param operatorUserId 操作人用户 ID
   * @param action 审批动作
   * @param comment 审批意见
   * @param variables 审批变量
   * @return 任务动作上下文
   */
  @Override
  public WorkflowTaskActionContext handleTask(String tenantId, String taskId, Long operatorUserId, String action,
      String comment, Map<String, Object> variables) {
    WorkflowTaskActionContext context = workflowMapper.selectTaskActionContext(tenantId, taskId,
        operatorUserId, action, comment, variables);
    if (context == null) {
      throw new IllegalArgumentException("工作流任务不存在、已处理或无权处理");
    }
    String handledStatus = ACTION_APPROVE.equals(action)
        ? WorkflowRuntimeStatus.APPROVED.getCode()
        : WorkflowRuntimeStatus.REJECTED.getCode();
    workflowMapper.updateTaskHandled(tenantId, taskId, operatorUserId, handledStatus, LocalDateTime.now());
    workflowMapper.insertApprovalRecord(tenantId, taskId, operatorUserId, action, comment);
    return context;
  }

  /**
   * 撤回流程实例并关闭未处理任务。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param operatorUserId 操作人用户 ID
   * @param reason 撤回原因
   */
  @Override
  public void revokeProcess(String tenantId, String processInstanceId, Long operatorUserId,
      String reason) {
    LocalDateTime revokedAt = LocalDateTime.now();
    workflowMapper.updateProcessRevoked(tenantId, processInstanceId, revokedAt);
    workflowMapper.updateProcessTodoTasksHandled(tenantId, processInstanceId,
        WorkflowRuntimeStatus.REVOKED.getCode(), revokedAt);
    workflowMapper.insertProcessApprovalRecord(tenantId, processInstanceId, operatorUserId,
        WorkflowRuntimeStatus.REVOKED.getCode(), reason);
  }
}
