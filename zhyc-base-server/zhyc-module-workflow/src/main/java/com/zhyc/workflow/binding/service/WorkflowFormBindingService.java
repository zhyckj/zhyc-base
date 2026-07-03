/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding.service;

import java.util.List;

/**
 * 工作流表单绑定业务服务。
 */
public interface WorkflowFormBindingService {

  /**
   * 查询租户内工作流表单绑定列表。
   *
   * @param tenantId 租户业务编码
   * @return 工作流表单绑定响应列表
   */
  List<WorkflowFormBindingResponse> listBindings(String tenantId);

  /**
   * 保存租户内工作流表单绑定。
   *
   * @param command 工作流表单绑定保存命令
   */
  void saveBinding(WorkflowFormBindingSaveCommand command);
}
