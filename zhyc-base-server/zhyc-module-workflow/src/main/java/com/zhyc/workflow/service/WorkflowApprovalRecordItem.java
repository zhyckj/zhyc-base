/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.service;

import java.time.LocalDateTime;

/**
 * 工作流审批记录列表项。
 */
public class WorkflowApprovalRecordItem {

  /** 任务 ID。 */
  private final String taskId;
  /** 操作用户 ID。 */
  private final Long operatorUserId;
  /** 审批动作。 */
  private final String action;
  /** 审批意见。 */
  private final String approvalComment;
  /** 操作时间。 */
  private final LocalDateTime operatedAt;

  /**
   * 创建工作流审批记录列表项。
   *
   * @param taskId 任务 ID
   * @param operatorUserId 操作用户 ID
   * @param action 审批动作
   * @param approvalComment 审批意见
   * @param operatedAt 操作时间
   */
  public WorkflowApprovalRecordItem(String taskId, Long operatorUserId, String action,
      String approvalComment, LocalDateTime operatedAt) {
    this.taskId = taskId;
    this.operatorUserId = operatorUserId;
    this.action = action;
    this.approvalComment = approvalComment;
    this.operatedAt = operatedAt;
  }

  public String getTaskId() {
    return taskId;
  }

  public Long getOperatorUserId() {
    return operatorUserId;
  }

  public String getAction() {
    return action;
  }

  public String getApprovalComment() {
    return approvalComment;
  }

  public LocalDateTime getOperatedAt() {
    return operatedAt;
  }
}
