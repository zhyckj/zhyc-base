/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.workflow.deployment.WorkflowModelDeploymentCommand;
import com.zhyc.workflow.deployment.WorkflowModelDeploymentResult;
import com.zhyc.workflow.deployment.WorkflowModelDeploymentService;
import com.zhyc.workflow.model.service.WorkflowProcessModelResponse;
import com.zhyc.workflow.model.service.WorkflowProcessModelSaveCommand;
import com.zhyc.workflow.model.service.WorkflowProcessModelService;
import com.zhyc.workflow.support.WorkflowServiceValidation;
import java.util.List;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作流流程模型管理接口。
 */
@RestController
@RequestMapping("/workflow/models")
public class WorkflowProcessModelController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 工作流流程模型业务服务。 */
  private final WorkflowProcessModelService modelService;
  /** 工作流流程模型发布服务提供器，未配置 Flowable 时允许模型基础接口继续启动。 */
  private final ObjectProvider<WorkflowModelDeploymentService> deploymentServiceProvider;

  /**
   * 创建工作流流程模型管理接口。
   *
   * @param modelService 工作流流程模型业务服务
   * @param deploymentServiceProvider 工作流流程模型发布服务提供器
   */
  public WorkflowProcessModelController(WorkflowProcessModelService modelService,
      ObjectProvider<WorkflowModelDeploymentService> deploymentServiceProvider) {
    this.modelService = Objects.requireNonNull(modelService, "工作流流程模型业务服务不能为空");
    this.deploymentServiceProvider = Objects.requireNonNull(deploymentServiceProvider,
        "工作流流程模型发布服务提供器不能为空");
  }

  /**
   * 查询租户内流程模型列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程模型响应列表
   */
  @RequiresPermissions("workflow:model:query")
  @GetMapping
  public ApiResult<List<WorkflowProcessModelResponse>> listModels(
      @RequestHeader(HEADER_TENANT_ID) String tenantId) {
    return ApiResult.ok(modelService.listModels(tenantId));
  }

  /**
   * 保存租户内流程模型。
   *
   * @param tenantId 租户业务编码
   * @param request 流程模型保存请求
   * @return 空响应
   */
  @RequiresPermissions("workflow:model:update")
  @PostMapping
  public ApiResult<Void> saveModel(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestBody WorkflowProcessModelSaveRequest request) {
    WorkflowProcessModelSaveRequest safeRequest = request == null
        ? new WorkflowProcessModelSaveRequest()
        : request;
    modelService.saveModel(new WorkflowProcessModelSaveCommand(tenantId, safeRequest.getId(),
        safeRequest.getModelCode(), safeRequest.getModelName(), safeRequest.getCategoryId(),
        safeRequest.getFlowableModelId(), safeRequest.getBpmnXml(), safeRequest.getStatus(),
        safeRequest.getRemark()));
    return ApiResult.ok(null);
  }

  /**
   * 发布租户内流程模型。
   *
   * <p>该接口需要 Flowable 仓储服务已配置，发布后会生成平台侧流程定义版本。</p>
   *
   * @param tenantId 租户业务编码
   * @param modelId 流程模型主键
   * @param request 流程模型发布请求
   * @return 流程模型发布结果
   */
  @RequiresPermissions("workflow:model:deploy")
  @PostMapping("/{modelId}/deploy")
  public ApiResult<WorkflowModelDeploymentResult> deployModel(
      @RequestHeader(HEADER_TENANT_ID) String tenantId, @PathVariable("modelId") Long modelId,
      @RequestBody WorkflowProcessModelDeployRequest request) {
    WorkflowProcessModelDeployRequest safeRequest = request == null
        ? new WorkflowProcessModelDeployRequest()
        : request;
    WorkflowModelDeploymentService deploymentService =
        deploymentServiceProvider.getIfAvailable(() -> {
          throw WorkflowServiceValidation.businessFailure("Flowable 发布服务未配置，不能发布流程模型");
        });
    WorkflowModelDeploymentResult result = deploymentService.deploy(
        new WorkflowModelDeploymentCommand(tenantId, modelId, safeRequest.getBpmnXml(),
            safeRequest.getRemark()));
    return ApiResult.ok(result);
  }
}
