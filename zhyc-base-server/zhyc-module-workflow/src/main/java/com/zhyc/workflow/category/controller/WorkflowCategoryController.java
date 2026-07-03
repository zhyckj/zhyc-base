/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.workflow.category.service.WorkflowCategoryResponse;
import com.zhyc.workflow.category.service.WorkflowCategorySaveCommand;
import com.zhyc.workflow.category.service.WorkflowCategoryService;
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
 * 工作流分类管理接口。
 */
@RestController
@RequestMapping("/workflow/categories")
public class WorkflowCategoryController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 工作流分类业务服务。 */
  private final WorkflowCategoryService categoryService;

  /**
   * 创建工作流分类管理接口。
   *
   * @param categoryService 工作流分类业务服务
   */
  public WorkflowCategoryController(WorkflowCategoryService categoryService) {
    this.categoryService = Objects.requireNonNull(categoryService,
        "工作流分类业务服务不能为空");
  }

  /**
   * 查询租户内工作流分类列表。
   *
   * @param tenantId 租户业务编码
   * @return 工作流分类响应列表
   */
  @RequiresPermissions("workflow:model:query")
  @GetMapping
  public ApiResult<List<WorkflowCategoryResponse>> listCategories(
      @RequestHeader(HEADER_TENANT_ID) String tenantId) {
    return ApiResult.ok(categoryService.listCategories(tenantId));
  }

  /**
   * 保存租户内工作流分类。
   *
   * @param tenantId 租户业务编码
   * @param request 工作流分类保存请求
   * @return 空响应
   */
  @RequiresPermissions("workflow:model:update")
  @PostMapping
  public ApiResult<Void> saveCategory(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestBody WorkflowCategorySaveRequest request) {
    WorkflowCategorySaveRequest safeRequest = request == null
        ? new WorkflowCategorySaveRequest()
        : request;
    categoryService.saveCategory(new WorkflowCategorySaveCommand(tenantId, safeRequest.getId(),
        safeRequest.getCategoryCode(), safeRequest.getCategoryName(), safeRequest.getSortOrder(),
        safeRequest.getStatus(), safeRequest.getRemark()));
    return ApiResult.ok(null);
  }
}
