/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category.service;

import com.zhyc.workflow.category.domain.WorkflowCategory;
import com.zhyc.workflow.category.repository.WorkflowCategoryRepository;
import com.zhyc.workflow.constant.WorkflowConfigStatus;
import com.zhyc.workflow.support.WorkflowServiceValidation;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认工作流分类业务服务实现。
 */
@Service
public class DefaultWorkflowCategoryService implements WorkflowCategoryService {

  /** 默认启用状态。 */
  private static final String DEFAULT_STATUS = WorkflowConfigStatus.ENABLED.getCode();

  /** 工作流分类仓储。 */
  private final WorkflowCategoryRepository categoryRepository;

  /**
   * 创建默认工作流分类业务服务。
   *
   * @param categoryRepository 工作流分类仓储
   */
  public DefaultWorkflowCategoryService(WorkflowCategoryRepository categoryRepository) {
    this.categoryRepository = Objects.requireNonNull(categoryRepository,
        "工作流分类仓储不能为空");
  }

  /**
   * 查询租户下的工作流分类。
   *
   * @param tenantId 租户业务编码
   * @return 工作流分类响应列表
   */
  @Override
  public List<WorkflowCategoryResponse> listCategories(String tenantId) {
    String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
    return categoryRepository.findByTenantId(requiredTenantId).stream()
        .sorted(Comparator
            .comparing(WorkflowCategory::getSortOrder, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(WorkflowCategory::getId, Comparator.nullsLast(Long::compareTo)))
        .map(this::toResponse)
        .toList();
  }

  /**
   * 保存工作流分类。
   *
   * @param command 工作流分类保存命令
   */
  @Override
  @Transactional
  public void saveCategory(WorkflowCategorySaveCommand command) {
    WorkflowCategorySaveCommand requiredCommand = WorkflowServiceValidation.requireObject(command,
        "工作流分类保存命令不能为空");
    WorkflowCategory category = new WorkflowCategory();
    category.setId(requiredCommand.getId());
    category.setTenantId(requireText(requiredCommand.getTenantId(), "租户业务编码不能为空"));
    category.setCategoryCode(requireText(requiredCommand.getCategoryCode(), "流程分类编码不能为空"));
    category.setCategoryName(requireText(requiredCommand.getCategoryName(), "流程分类名称不能为空"));
    category.setSortOrder(requiredCommand.getSortOrder() == null ? 0 : requiredCommand.getSortOrder());
    category.setStatus(normalizeStatus(defaultText(requiredCommand.getStatus(), DEFAULT_STATUS)));
    category.setRemark(trimToNull(requiredCommand.getRemark()));
    categoryRepository.save(category);
  }

  /**
   * 转换为响应对象。
   *
   * @param category 工作流分类模型
   * @return 工作流分类响应对象
   */
  private WorkflowCategoryResponse toResponse(WorkflowCategory category) {
    return new WorkflowCategoryResponse(category.getId(), category.getTenantId(),
        category.getCategoryCode(), category.getCategoryName(), category.getSortOrder(),
        category.getStatus(), category.getCreatedAt(), category.getUpdatedAt(), category.getRemark());
  }

  /**
   * 校验文本不能为空并去除首尾空白。
   *
   * @param value 原始文本
   * @param message 为空时的异常消息
   * @return 清理后的文本
   */
  private String requireText(String value, String message) {
    String trimmed = trimToNull(value);
    if (trimmed == null) {
      throw WorkflowServiceValidation.businessFailure(message);
    }
    return trimmed;
  }

  /**
   * 返回非空文本或默认值。
   *
   * @param value 原始文本
   * @param defaultValue 默认文本
   * @return 清理后的文本或默认文本
   */
  private String defaultText(String value, String defaultValue) {
    String trimmed = trimToNull(value);
    return trimmed == null ? defaultValue : trimmed;
  }

  /**
   * 校验并规范化工作流配置状态。
   *
   * @param status 配置状态编码
   * @return 规范化后的配置状态编码
   */
  private String normalizeStatus(String status) {
    try {
      return WorkflowConfigStatus.fromCode(status).getCode();
    } catch (IllegalArgumentException exception) {
      throw WorkflowServiceValidation.businessFailure(exception.getMessage());
    }
  }

  /**
   * 将空白文本转换为 null。
   *
   * @param value 原始文本
   * @return 清理后的文本
   */
  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
