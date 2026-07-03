/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding.mapper;

import com.zhyc.workflow.binding.domain.WorkflowFormBinding;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 工作流表单绑定 MyBatis Mapper。
 */
@Mapper
public interface WorkflowFormBindingMapper {

  /**
   * 查询租户内工作流表单绑定列表。
   *
   * @param tenantId 租户业务编码
   * @return 工作流表单绑定列表
   */
  @SelectProvider(type = WorkflowFormBindingSqlProvider.class, method = "selectByTenantId")
  List<WorkflowFormBinding> selectByTenantId(@Param("tenantId") String tenantId);

  /**
   * 新增或更新工作流表单绑定。
   *
   * @param binding 工作流表单绑定模型
   */
  @InsertProvider(type = WorkflowFormBindingSqlProvider.class, method = "upsertBinding")
  void upsertBinding(WorkflowFormBinding binding);
}
