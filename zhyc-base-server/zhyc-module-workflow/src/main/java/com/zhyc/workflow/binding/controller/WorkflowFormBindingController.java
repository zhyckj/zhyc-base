/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.workflow.binding.service.WorkflowFormBindingResponse;
import com.zhyc.workflow.binding.service.WorkflowFormBindingSaveCommand;
import com.zhyc.workflow.binding.service.WorkflowFormBindingService;
import java.util.List;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作流表单绑定管理接口。
 */
@RestController
@RequestMapping("/workflow/form-bindings")
public class WorkflowFormBindingController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 工作流表单绑定业务服务。 */
  private final WorkflowFormBindingService bindingService;

  /**
   * 创建工作流表单绑定管理接口。
   *
   * @param bindingService 工作流表单绑定业务服务
   */
  public WorkflowFormBindingController(WorkflowFormBindingService bindingService) {
    this.bindingService = Objects.requireNonNull(bindingService,
        "工作流表单绑定业务服务不能为空");
  }

  /**
   * 查询租户内工作流表单绑定列表。
   *
   * @param tenantId 租户业务编码
   * @return 工作流表单绑定响应列表
   */
  @RequiresPermissions("workflow:binding:query")
  @GetMapping
  public ApiResult<List<WorkflowFormBindingResponse>> listBindings(
      @RequestHeader(HEADER_TENANT_ID) String tenantId) {
    return ApiResult.ok(bindingService.listBindings(tenantId));
  }

  /**
   * 保存租户内工作流表单绑定。
   *
   * @param tenantId 租户业务编码
   * @param request 工作流表单绑定保存请求
   * @return 空响应
   */
  @RequiresPermissions("workflow:binding:update")
  @PostMapping
  public ApiResult<Void> saveBinding(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestBody WorkflowFormBindingSaveRequest request) {
    WorkflowFormBindingSaveRequest safeRequest = request == null
        ? new WorkflowFormBindingSaveRequest()
        : request;
    bindingService.saveBinding(new WorkflowFormBindingSaveCommand(tenantId, safeRequest.getId(),
        safeRequest.getProcessKey(), safeRequest.getBusinessModule(), safeRequest.getBusinessTable(),
        safeRequest.getFormRoute(), safeRequest.getMobileRoute(), safeRequest.getStatus(),
        safeRequest.getRemark()));
    return ApiResult.ok(null);
  }
}
