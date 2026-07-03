/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition.repository;

import com.zhyc.workflow.definition.domain.WorkflowDefinition;
import com.zhyc.workflow.definition.mapper.WorkflowDefinitionMapper;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的工作流流程定义仓储实现。
 */
@Repository
public class MyBatisWorkflowDefinitionRepository implements WorkflowDefinitionRepository {

  /** 工作流流程定义 Mapper。 */
  private final WorkflowDefinitionMapper definitionMapper;

  /**
   * 创建工作流流程定义仓储实现。
   *
   * @param definitionMapper 工作流流程定义 Mapper
   */
  public MyBatisWorkflowDefinitionRepository(WorkflowDefinitionMapper definitionMapper) {
    this.definitionMapper = Objects.requireNonNull(definitionMapper,
        "工作流流程定义 Mapper 不能为空");
  }

  /**
   * 查询租户下的流程定义。
   *
   * @param tenantId 租户业务编码
   * @return 流程定义列表
   */
  @Override
  public List<WorkflowDefinition> findByTenantId(String tenantId) {
    return definitionMapper.selectByTenantId(tenantId);
  }

  /**
   * 保存或更新流程定义。
   *
   * @param definition 工作流流程定义模型
   */
  @Override
  public void save(WorkflowDefinition definition) {
    definitionMapper.upsertDefinition(definition);
  }
}
