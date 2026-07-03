/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model.repository;

import com.zhyc.workflow.model.domain.WorkflowProcessModel;
import com.zhyc.workflow.model.mapper.WorkflowProcessModelMapper;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的工作流流程模型仓储实现。
 */
@Repository
public class MyBatisWorkflowProcessModelRepository implements WorkflowProcessModelRepository {

  /** 工作流流程模型 Mapper。 */
  private final WorkflowProcessModelMapper modelMapper;

  /**
   * 创建工作流流程模型仓储实现。
   *
   * @param modelMapper 工作流流程模型 Mapper
   */
  public MyBatisWorkflowProcessModelRepository(WorkflowProcessModelMapper modelMapper) {
    this.modelMapper = Objects.requireNonNull(modelMapper, "工作流流程模型 Mapper 不能为空");
  }

  /**
   * 查询租户下的流程模型。
   *
   * @param tenantId 租户业务编码
   * @return 流程模型列表
   */
  @Override
  public List<WorkflowProcessModel> findByTenantId(String tenantId) {
    return modelMapper.selectByTenantId(tenantId);
  }

  /**
   * 保存或更新流程模型。
   *
   * @param model 工作流流程模型
   */
  @Override
  public void save(WorkflowProcessModel model) {
    modelMapper.upsertModel(model);
  }
}
