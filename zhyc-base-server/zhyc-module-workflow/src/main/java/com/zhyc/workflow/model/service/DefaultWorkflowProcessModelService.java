/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model.service;

import com.zhyc.workflow.constant.WorkflowConfigStatus;
import com.zhyc.workflow.model.domain.WorkflowProcessModel;
import com.zhyc.workflow.model.repository.WorkflowProcessModelRepository;
import com.zhyc.workflow.support.WorkflowServiceValidation;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认工作流流程模型业务服务实现。
 */
@Service
public class DefaultWorkflowProcessModelService implements WorkflowProcessModelService {

  /** 默认启用状态。 */
  private static final String DEFAULT_STATUS = WorkflowConfigStatus.ENABLED.getCode();

  /** 工作流流程模型仓储。 */
  private final WorkflowProcessModelRepository modelRepository;

  /**
   * 创建默认工作流流程模型业务服务。
   *
   * @param modelRepository 工作流流程模型仓储
   */
  public DefaultWorkflowProcessModelService(WorkflowProcessModelRepository modelRepository) {
    this.modelRepository = Objects.requireNonNull(modelRepository, "工作流流程模型仓储不能为空");
  }

  /**
   * 查询租户下的流程模型。
   *
   * @param tenantId 租户业务编码
   * @return 流程模型响应列表
   */
  @Override
  public List<WorkflowProcessModelResponse> listModels(String tenantId) {
    String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
    return modelRepository.findByTenantId(requiredTenantId).stream()
        .sorted(Comparator
            .comparing(WorkflowProcessModel::getCategoryId,
                Comparator.nullsLast(Long::compareTo))
            .thenComparing(WorkflowProcessModel::getModelCode,
                Comparator.nullsLast(String::compareTo)))
        .map(this::toResponse)
        .toList();
  }

  /**
   * 保存流程模型。
   *
   * <p>流程模型用于关联平台模型编码与 Flowable 模型 ID，发布前必须归属当前租户。</p>
   *
   * @param command 流程模型保存命令
   */
  @Override
  @Transactional
  public void saveModel(WorkflowProcessModelSaveCommand command) {
    WorkflowProcessModelSaveCommand requiredCommand = WorkflowServiceValidation.requireObject(command,
        "工作流流程模型保存命令不能为空");
    WorkflowProcessModel model = new WorkflowProcessModel();
    model.setId(requiredCommand.getId());
    model.setTenantId(requireText(requiredCommand.getTenantId(), "租户业务编码不能为空"));
    model.setCategoryId(requiredCommand.getCategoryId());
    String requiredModelCode = requireText(requiredCommand.getModelCode(), "流程模型编码不能为空");
    model.setModelCode(requiredModelCode);
    model.setModelName(requireText(requiredCommand.getModelName(), "流程模型名称不能为空"));
    model.setFlowableModelId(defaultText(requiredCommand.getFlowableModelId(), requiredModelCode));
    model.setBpmnXml(trimToNull(requiredCommand.getBpmnXml()));
    model.setStatus(normalizeStatus(defaultText(requiredCommand.getStatus(), DEFAULT_STATUS)));
    model.setRemark(trimToNull(requiredCommand.getRemark()));
    modelRepository.save(model);
  }

  /**
   * 转换为响应对象。
   *
   * @param model 工作流流程模型
   * @return 工作流流程模型响应对象
   */
  private WorkflowProcessModelResponse toResponse(WorkflowProcessModel model) {
    return new WorkflowProcessModelResponse(model.getId(), model.getTenantId(),
        model.getModelCode(), model.getModelName(), model.getCategoryId(),
        model.getFlowableModelId(), model.getBpmnXml(), model.getStatus(), model.getCreatedAt(),
        model.getUpdatedAt(), model.getRemark());
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
