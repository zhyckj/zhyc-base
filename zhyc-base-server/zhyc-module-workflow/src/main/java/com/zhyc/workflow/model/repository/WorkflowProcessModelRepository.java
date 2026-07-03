/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model.repository;

import com.zhyc.workflow.model.domain.WorkflowProcessModel;
import java.util.List;

/**
 * 工作流流程模型仓储。
 */
public interface WorkflowProcessModelRepository {

  /**
   * 查询租户内流程模型列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程模型列表
   */
  List<WorkflowProcessModel> findByTenantId(String tenantId);

  /**
   * 保存流程模型。
   *
   * @param model 流程模型
   */
  void save(WorkflowProcessModel model);
}
