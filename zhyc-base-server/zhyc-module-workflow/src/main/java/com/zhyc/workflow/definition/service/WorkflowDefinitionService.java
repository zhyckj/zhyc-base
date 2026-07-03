/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition.service;

import java.util.List;

/**
 * 工作流流程定义业务服务。
 */
public interface WorkflowDefinitionService {

  /**
   * 查询租户内流程定义版本列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程定义响应列表
   */
  List<WorkflowDefinitionResponse> listDefinitions(String tenantId);

  /**
   * 保存租户内流程定义版本。
   *
   * @param command 流程定义保存命令
   */
  void saveDefinition(WorkflowDefinitionSaveCommand command);
}
