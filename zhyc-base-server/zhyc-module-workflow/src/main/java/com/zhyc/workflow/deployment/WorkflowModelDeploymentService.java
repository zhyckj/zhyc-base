/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.deployment;

/**
 * 工作流流程模型发布服务。
 *
 * <p>封装平台流程模型到 Flowable 部署的转换边界，业务模块不得直接调用 Flowable 发布 API。</p>
 */
public interface WorkflowModelDeploymentService {

  /**
   * 发布流程模型。
   *
   * <p>校验租户和模型状态，部署 BPMN 资源到 Flowable，并保存平台侧流程定义版本。</p>
   *
   * @param command 流程模型发布命令
   * @return 流程模型发布结果
   */
  WorkflowModelDeploymentResult deploy(WorkflowModelDeploymentCommand command);
}
