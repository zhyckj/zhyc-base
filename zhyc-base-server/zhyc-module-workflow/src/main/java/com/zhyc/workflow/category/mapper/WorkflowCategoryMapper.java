/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category.mapper;

import com.zhyc.workflow.category.domain.WorkflowCategory;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 工作流分类 MyBatis Mapper。
 */
@Mapper
public interface WorkflowCategoryMapper {

  /**
   * 查询租户内工作流分类列表。
   *
   * @param tenantId 租户业务编码
   * @return 工作流分类列表
   */
  @SelectProvider(type = WorkflowCategorySqlProvider.class, method = "selectByTenantId")
  List<WorkflowCategory> selectByTenantId(@Param("tenantId") String tenantId);

  /**
   * 新增或更新工作流分类。
   *
   * @param category 工作流分类模型
   */
  @InsertProvider(type = WorkflowCategorySqlProvider.class, method = "upsertCategory")
  void upsertCategory(WorkflowCategory category);
}
