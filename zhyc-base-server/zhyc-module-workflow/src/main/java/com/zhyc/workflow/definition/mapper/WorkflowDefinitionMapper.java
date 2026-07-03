/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition.mapper;

import com.zhyc.workflow.definition.domain.WorkflowDefinition;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 工作流流程定义 MyBatis Mapper。
 */
@Mapper
public interface WorkflowDefinitionMapper {

  /**
   * 查询租户内流程定义版本列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程定义版本列表
   */
  @SelectProvider(type = WorkflowDefinitionSqlProvider.class, method = "selectByTenantId")
  List<WorkflowDefinition> selectByTenantId(@Param("tenantId") String tenantId);

  /**
   * 新增或更新流程定义版本。
   *
   * @param definition 流程定义模型
   */
  @InsertProvider(type = WorkflowDefinitionSqlProvider.class, method = "upsertDefinition")
  void upsertDefinition(WorkflowDefinition definition);
}
