/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category.repository;

import com.zhyc.workflow.category.domain.WorkflowCategory;
import java.util.List;

/**
 * 工作流分类仓储。
 */
public interface WorkflowCategoryRepository {

  /**
   * 查询租户内工作流分类列表。
   *
   * @param tenantId 租户业务编码
   * @return 工作流分类列表
   */
  List<WorkflowCategory> findByTenantId(String tenantId);

  /**
   * 保存工作流分类。
   *
   * @param category 工作流分类模型
   */
  void save(WorkflowCategory category);
}
