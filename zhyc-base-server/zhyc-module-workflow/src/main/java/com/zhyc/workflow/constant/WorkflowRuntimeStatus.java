/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.constant;

/**
 * 工作流运行状态枚举。
 *
 * <p>用于统一流程实例和任务运行状态编码，避免 SQL、仓储和服务层散落魔法字符串。</p>
 */
public enum WorkflowRuntimeStatus {

  /** 流程实例运行中，表示流程尚未结束且仍允许处理待办任务。 */
  RUNNING("RUNNING", "流程实例运行中"),
  /** 任务待处理，表示当前任务仍可被审批人执行审批动作。 */
  TODO("TODO", "任务待处理"),
  /** 任务已审批通过，表示当前审批节点已完成且结果为通过。 */
  APPROVED("APPROVED", "任务已审批通过"),
  /** 任务已驳回，表示当前审批节点已完成且结果为驳回。 */
  REJECTED("REJECTED", "任务已驳回"),
  /** 流程或任务已撤回，表示业务侧撤回流程后未办任务被终止。 */
  REVOKED("REVOKED", "流程或任务已撤回");

  /** 持久化到数据库的稳定状态编码。 */
  private final String code;

  /** 状态中文说明，用于后台、移动端和审计记录展示。 */
  private final String description;

  /**
   * 创建工作流运行状态。
   *
   * @param code 持久化状态编码
   * @param description 状态中文说明
   */
  WorkflowRuntimeStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 返回持久化状态编码。
   *
   * @return 状态编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 返回状态中文说明。
   *
   * @return 状态中文说明
   */
  public String getDescription() {
    return description;
  }

  /**
   * 返回 SQL Provider 可直接拼入固定 SQL 的状态字面量。
   *
   * @return 带单引号的 SQL 状态字面量
   */
  public String sqlLiteral() {
    return "'" + code + "'";
  }

  /**
   * 根据持久化编码解析工作流运行状态。
   *
   * <p>用于数据库值、接口值和审计值回到领域枚举；未知编码会被明确拒绝，避免静默落入错误状态。</p>
   *
   * @param code 持久化状态编码
   * @return 工作流运行状态枚举
   */
  public static WorkflowRuntimeStatus fromCode(String code) {
    for (WorkflowRuntimeStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("不支持的工作流运行状态编码: " + code);
  }
}
