/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.service;

/**
 * 工作流流程撤回命令。
 */
public class WorkflowProcessRevokeCommand {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 流程实例 ID。 */
  private final String processInstanceId;
  /** 当前操作用户 ID。 */
  private final Long operatorUserId;
  /** 撤回原因。 */
  private final String reason;

  /**
   * 创建工作流流程撤回命令。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param operatorUserId 当前操作用户 ID
   * @param reason 撤回原因
   */
  public WorkflowProcessRevokeCommand(String tenantId, String processInstanceId,
      Long operatorUserId, String reason) {
    this.tenantId = tenantId;
    this.processInstanceId = processInstanceId;
    this.operatorUserId = operatorUserId;
    this.reason = reason;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public Long getOperatorUserId() {
    return operatorUserId;
  }

  public String getReason() {
    return reason;
  }
}
