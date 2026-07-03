/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition.repository;

import com.zhyc.workflow.definition.domain.WorkflowDefinition;
import java.util.List;

/**
 * 工作流流程定义仓储。
 */
public interface WorkflowDefinitionRepository {

  /**
   * 查询租户内流程定义版本列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程定义版本列表
   */
  List<WorkflowDefinition> findByTenantId(String tenantId);

  /**
   * 保存流程定义版本。
   *
   * @param definition 流程定义模型
   */
  void save(WorkflowDefinition definition);
}
