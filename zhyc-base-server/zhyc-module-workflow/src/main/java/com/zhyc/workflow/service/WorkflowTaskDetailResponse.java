/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作流任务详情响应。
 */
public class WorkflowTaskDetailResponse {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 任务 ID。 */
  private final String taskId;
  /** 流程实例 ID。 */
  private final String processInstanceId;
  /** 流程定义 key。 */
  private final String processKey;
  /** 任务名称。 */
  private final String taskName;
  /** 业务对象唯一标识。 */
  private final String businessKey;
  /** 任务处理人用户 ID。 */
  private final Long assigneeUserId;
  /** 任务状态。 */
  private final String status;
  /** 任务创建时间。 */
  private final LocalDateTime createdAt;
  /** 审批记录列表。 */
  private final List<WorkflowApprovalRecordItem> approvalRecords;

  /**
   * 创建工作流任务详情响应。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param processInstanceId 流程实例 ID
   * @param processKey 流程定义 key
   * @param taskName 任务名称
   * @param businessKey 业务对象唯一标识
   * @param assigneeUserId 任务处理人用户 ID
   * @param status 任务状态
   * @param createdAt 任务创建时间
   * @param approvalRecords 审批记录列表
   */
  public WorkflowTaskDetailResponse(String tenantId, String taskId, String processInstanceId,
      String processKey, String taskName, String businessKey, Long assigneeUserId, String status,
      LocalDateTime createdAt, List<WorkflowApprovalRecordItem> approvalRecords) {
    this.tenantId = tenantId;
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    this.processKey = processKey;
    this.taskName = taskName;
    this.businessKey = businessKey;
    this.assigneeUserId = assigneeUserId;
    this.status = status;
    this.createdAt = createdAt;
    this.approvalRecords = approvalRecords == null ? List.of() : List.copyOf(approvalRecords);
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessKey() {
    return processKey;
  }

  public String getTaskName() {
    return taskName;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public Long getAssigneeUserId() {
    return assigneeUserId;
  }

  public String getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public List<WorkflowApprovalRecordItem> getApprovalRecords() {
    return approvalRecords;
  }
}
