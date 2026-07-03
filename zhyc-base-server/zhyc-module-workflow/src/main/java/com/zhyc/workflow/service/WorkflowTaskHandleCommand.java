/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.service;

import java.util.Map;

/**
 * 工作流任务处理命令。
 */
public class WorkflowTaskHandleCommand {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 任务 ID。 */
  private final String taskId;
  /** 当前操作用户 ID。 */
  private final Long operatorUserId;
  /** 审批意见。 */
  private final String comment;
  /** 任务处理变量。 */
  private final Map<String, Object> variables;

  /**
   * 创建工作流任务处理命令。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param operatorUserId 当前操作用户 ID
   * @param comment 审批意见
   * @param variables 任务处理变量
   */
  public WorkflowTaskHandleCommand(String tenantId, String taskId, Long operatorUserId,
      String comment, Map<String, Object> variables) {
    this.tenantId = tenantId;
    this.taskId = taskId;
    this.operatorUserId = operatorUserId;
    this.comment = comment;
    this.variables = variables;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getTaskId() {
    return taskId;
  }

  public Long getOperatorUserId() {
    return operatorUserId;
  }

  public String getComment() {
    return comment;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }
}
