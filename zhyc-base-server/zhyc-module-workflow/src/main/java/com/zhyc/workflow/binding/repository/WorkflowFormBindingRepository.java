/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding.repository;

import com.zhyc.workflow.binding.domain.WorkflowFormBinding;
import java.util.List;

/**
 * 工作流表单绑定仓储。
 */
public interface WorkflowFormBindingRepository {

  /**
   * 查询租户内工作流表单绑定列表。
   *
   * @param tenantId 租户业务编码
   * @return 工作流表单绑定列表
   */
  List<WorkflowFormBinding> findByTenantId(String tenantId);

  /**
   * 保存工作流表单绑定。
   *
   * @param binding 工作流表单绑定模型
   */
  void save(WorkflowFormBinding binding);
}
