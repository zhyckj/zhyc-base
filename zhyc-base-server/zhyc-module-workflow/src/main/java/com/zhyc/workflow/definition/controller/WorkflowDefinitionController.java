/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.workflow.definition.service.WorkflowDefinitionResponse;
import com.zhyc.workflow.definition.service.WorkflowDefinitionSaveCommand;
import com.zhyc.workflow.definition.service.WorkflowDefinitionService;
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
 * 工作流流程定义管理接口。
 */
@RestController
@RequestMapping("/workflow/definitions")
public class WorkflowDefinitionController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 工作流流程定义业务服务。 */
  private final WorkflowDefinitionService definitionService;

  /**
   * 创建工作流流程定义管理接口。
   *
   * @param definitionService 工作流流程定义业务服务
   */
  public WorkflowDefinitionController(WorkflowDefinitionService definitionService) {
    this.definitionService = Objects.requireNonNull(definitionService,
        "工作流流程定义业务服务不能为空");
  }

  /**
   * 查询租户内流程定义版本列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程定义响应列表
   */
  @RequiresPermissions("workflow:model:query")
  @GetMapping
  public ApiResult<List<WorkflowDefinitionResponse>> listDefinitions(
      @RequestHeader(HEADER_TENANT_ID) String tenantId) {
    return ApiResult.ok(definitionService.listDefinitions(tenantId));
  }

  /**
   * 保存租户内流程定义版本。
   *
   * @param tenantId 租户业务编码
   * @param request 流程定义保存请求
   * @return 空响应
   */
  @RequiresPermissions("workflow:model:deploy")
  @PostMapping
  public ApiResult<Void> saveDefinition(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestBody WorkflowDefinitionSaveRequest request) {
    WorkflowDefinitionSaveRequest safeRequest = request == null
        ? new WorkflowDefinitionSaveRequest()
        : request;
    definitionService.saveDefinition(new WorkflowDefinitionSaveCommand(tenantId, safeRequest.getId(),
        safeRequest.getProcessKey(), safeRequest.getProcessName(), safeRequest.getVersion(),
        safeRequest.getDeploymentId(), safeRequest.getStatus(), safeRequest.getRemark()));
    return ApiResult.ok(null);
  }
}
