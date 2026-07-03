/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.workflow;

import java.util.Map;

/**
 * 工作流任务动作上下文。
 *
 * <p>用于工作流模块向业务模块回传任务处理结果，业务模块只能依赖该公共契约，不得依赖工作流实现模块。</p>
 */
public class WorkflowTaskActionContext {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 任务 ID。 */
  private final String taskId;
  /** 流程实例 ID。 */
  private final String processInstanceId;
  /** 流程定义 key。 */
  private final String processKey;
  /** 业务对象唯一标识。 */
  private final String businessKey;
  /** 操作用户 ID。 */
  private final Long operatorUserId;
  /** 任务处理动作。 */
  private final String action;
  /** 审批意见。 */
  private final String comment;
  /** 任务处理变量。 */
  private final Map<String, Object> variables;

  /**
   * 创建工作流任务动作上下文。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param processInstanceId 流程实例 ID
   * @param processKey 流程定义 key
   * @param businessKey 业务对象唯一标识
   * @param operatorUserId 操作用户 ID
   * @param action 任务处理动作
   * @param comment 审批意见
   * @param variables 任务处理变量
   */
  public WorkflowTaskActionContext(String tenantId, String taskId, String processInstanceId,
      String processKey, String businessKey, Long operatorUserId, String action, String comment,
      Map<String, Object> variables) {
    this.tenantId = tenantId;
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    this.processKey = processKey;
    this.businessKey = businessKey;
    this.operatorUserId = operatorUserId;
    this.action = action;
    this.comment = comment;
    this.variables = variables;
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

  public String getBusinessKey() {
    return businessKey;
  }

  public Long getOperatorUserId() {
    return operatorUserId;
  }

  public String getAction() {
    return action;
  }

  public String getComment() {
    return comment;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }
}
