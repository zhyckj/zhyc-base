/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.workflow;

/**
 * 工作流任务动作处理器。
 *
 * <p>业务模块通过实现该接口响应审批通过、驳回等动作，工作流模块只依赖公共契约回调业务状态。</p>
 */
public interface WorkflowTaskActionHandler {

  /**
   * 处理工作流任务动作。
   *
   * @param context 工作流任务动作上下文
   */
  void handle(WorkflowTaskActionContext context);
}
