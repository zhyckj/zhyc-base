/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category.repository;

import com.zhyc.workflow.category.domain.WorkflowCategory;
import com.zhyc.workflow.category.mapper.WorkflowCategoryMapper;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的工作流分类仓储实现。
 */
@Repository
public class MyBatisWorkflowCategoryRepository implements WorkflowCategoryRepository {

  /** 工作流分类 Mapper。 */
  private final WorkflowCategoryMapper categoryMapper;

  /**
   * 创建工作流分类仓储实现。
   *
   * @param categoryMapper 工作流分类 Mapper
   */
  public MyBatisWorkflowCategoryRepository(WorkflowCategoryMapper categoryMapper) {
    this.categoryMapper = Objects.requireNonNull(categoryMapper,
        "工作流分类 Mapper 不能为空");
  }

  /**
   * 查询租户下的工作流分类。
   *
   * @param tenantId 租户业务编码
   * @return 工作流分类列表
   */
  @Override
  public List<WorkflowCategory> findByTenantId(String tenantId) {
    return categoryMapper.selectByTenantId(tenantId);
  }

  /**
   * 保存或更新工作流分类。
   *
   * @param category 工作流分类模型
   */
  @Override
  public void save(WorkflowCategory category) {
    categoryMapper.upsertCategory(category);
  }
}
