/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding.repository;

import com.zhyc.workflow.binding.domain.WorkflowFormBinding;
import com.zhyc.workflow.binding.mapper.WorkflowFormBindingMapper;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的工作流表单绑定仓储实现。
 */
@Repository
public class MyBatisWorkflowFormBindingRepository implements WorkflowFormBindingRepository {

  /** 工作流表单绑定 Mapper。 */
  private final WorkflowFormBindingMapper bindingMapper;

  /**
   * 创建工作流表单绑定仓储实现。
   *
   * @param bindingMapper 工作流表单绑定 Mapper
   */
  public MyBatisWorkflowFormBindingRepository(WorkflowFormBindingMapper bindingMapper) {
    this.bindingMapper = Objects.requireNonNull(bindingMapper,
        "工作流表单绑定 Mapper 不能为空");
  }

  /**
   * 查询租户下的工作流表单绑定。
   *
   * @param tenantId 租户业务编码
   * @return 工作流表单绑定列表
   */
  @Override
  public List<WorkflowFormBinding> findByTenantId(String tenantId) {
    return bindingMapper.selectByTenantId(tenantId);
  }

  /**
   * 保存或更新工作流表单绑定。
   *
   * @param binding 工作流表单绑定模型
   */
  @Override
  public void save(WorkflowFormBinding binding) {
    bindingMapper.upsertBinding(binding);
  }
}
