/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding.service;

import com.zhyc.workflow.binding.domain.WorkflowFormBinding;
import com.zhyc.workflow.binding.repository.WorkflowFormBindingRepository;
import com.zhyc.workflow.constant.WorkflowConfigStatus;
import com.zhyc.workflow.support.WorkflowServiceValidation;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认工作流表单绑定业务服务实现。
 */
@Service
public class DefaultWorkflowFormBindingService implements WorkflowFormBindingService {

  /** 默认启用状态。 */
  private static final String DEFAULT_STATUS = WorkflowConfigStatus.ENABLED.getCode();

  /** 工作流表单绑定仓储。 */
  private final WorkflowFormBindingRepository bindingRepository;

  /**
   * 创建默认工作流表单绑定业务服务。
   *
   * @param bindingRepository 工作流表单绑定仓储
   */
  public DefaultWorkflowFormBindingService(WorkflowFormBindingRepository bindingRepository) {
    this.bindingRepository = Objects.requireNonNull(bindingRepository,
        "工作流表单绑定仓储不能为空");
  }

  /**
   * 查询租户下的工作流表单绑定。
   *
   * @param tenantId 租户业务编码
   * @return 工作流表单绑定响应列表
   */
  @Override
  public List<WorkflowFormBindingResponse> listBindings(String tenantId) {
    String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
    return bindingRepository.findByTenantId(requiredTenantId).stream()
        .sorted(Comparator
            .comparing(WorkflowFormBinding::getBusinessModule, Comparator.nullsLast(String::compareTo))
            .thenComparing(WorkflowFormBinding::getProcessKey, Comparator.nullsLast(String::compareTo)))
        .map(this::toResponse)
        .toList();
  }

  /**
   * 保存工作流表单绑定。
   *
   * <p>绑定记录用于把流程定义与后台表单路由、移动端路由关联，支撑审批详情跳转。</p>
   *
   * @param command 工作流表单绑定保存命令
   */
  @Override
  @Transactional
  public void saveBinding(WorkflowFormBindingSaveCommand command) {
    WorkflowFormBindingSaveCommand requiredCommand = WorkflowServiceValidation.requireObject(command,
        "工作流表单绑定保存命令不能为空");
    WorkflowFormBinding binding = new WorkflowFormBinding();
    binding.setId(requiredCommand.getId());
    binding.setTenantId(requireText(requiredCommand.getTenantId(), "租户业务编码不能为空"));
    binding.setProcessKey(requireText(requiredCommand.getProcessKey(), "流程定义 key 不能为空"));
    binding.setBusinessModule(requireText(requiredCommand.getBusinessModule(), "业务模块编码不能为空"));
    binding.setBusinessTable(requireText(requiredCommand.getBusinessTable(), "业务表名不能为空"));
    binding.setFormRoute(requireText(requiredCommand.getFormRoute(), "后台表单路由不能为空"));
    binding.setMobileRoute(trimToNull(requiredCommand.getMobileRoute()));
    binding.setStatus(normalizeStatus(defaultText(requiredCommand.getStatus(), DEFAULT_STATUS)));
    binding.setRemark(trimToNull(requiredCommand.getRemark()));
    bindingRepository.save(binding);
  }

  /**
   * 转换为响应对象。
   *
   * @param binding 工作流表单绑定模型
   * @return 工作流表单绑定响应对象
   */
  private WorkflowFormBindingResponse toResponse(WorkflowFormBinding binding) {
    return new WorkflowFormBindingResponse(binding.getId(), binding.getTenantId(),
        binding.getProcessKey(), binding.getBusinessModule(), binding.getBusinessTable(),
        binding.getFormRoute(), binding.getMobileRoute(), binding.getStatus(),
        binding.getCreatedAt(), binding.getUpdatedAt(), binding.getRemark());
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
