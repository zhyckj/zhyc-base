/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model.mapper;

import com.zhyc.workflow.model.domain.WorkflowProcessModel;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 工作流流程模型 MyBatis Mapper。
 */
@Mapper
public interface WorkflowProcessModelMapper {

  /**
   * 查询租户内流程模型列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程模型列表
   */
  @SelectProvider(type = WorkflowProcessModelSqlProvider.class, method = "selectByTenantId")
  List<WorkflowProcessModel> selectByTenantId(@Param("tenantId") String tenantId);

  /**
   * 新增或更新流程模型。
   *
   * @param model 流程模型
   */
  @InsertProvider(type = WorkflowProcessModelSqlProvider.class, method = "upsertModel")
  void upsertModel(WorkflowProcessModel model);
}
