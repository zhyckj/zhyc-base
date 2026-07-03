/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition.service;

import com.zhyc.workflow.constant.WorkflowDefinitionStatus;
import com.zhyc.workflow.definition.domain.WorkflowDefinition;
import com.zhyc.workflow.definition.repository.WorkflowDefinitionRepository;
import com.zhyc.workflow.support.WorkflowServiceValidation;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认工作流流程定义业务服务实现。
 */
@Service
public class DefaultWorkflowDefinitionService implements WorkflowDefinitionService {

  /** 默认启用状态。 */
  private static final String DEFAULT_STATUS = WorkflowDefinitionStatus.ACTIVE.getCode();

  /** 工作流流程定义仓储。 */
  private final WorkflowDefinitionRepository definitionRepository;

  /**
   * 创建默认工作流流程定义业务服务。
   *
   * @param definitionRepository 工作流流程定义仓储
   */
  public DefaultWorkflowDefinitionService(WorkflowDefinitionRepository definitionRepository) {
    this.definitionRepository = Objects.requireNonNull(definitionRepository,
        "工作流流程定义仓储不能为空");
  }

  /**
   * 查询租户下的流程定义。
   *
   * @param tenantId 租户业务编码
   * @return 流程定义响应列表
   */
  @Override
  public List<WorkflowDefinitionResponse> listDefinitions(String tenantId) {
    String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
    return definitionRepository.findByTenantId(requiredTenantId).stream()
        .sorted(Comparator
            .comparing(WorkflowDefinition::getProcessKey, Comparator.nullsLast(String::compareTo))
            .thenComparing(WorkflowDefinition::getVersion,
                Comparator.nullsLast(Comparator.reverseOrder())))
        .map(this::toResponse)
        .toList();
  }

  /**
   * 保存流程定义。
   *
   * <p>流程定义保存时必须绑定租户、流程 key、版本和 Flowable 部署 ID，支撑后续任务路由与表单绑定。</p>
   *
   * @param command 流程定义保存命令
   */
  @Override
  @Transactional
  public void saveDefinition(WorkflowDefinitionSaveCommand command) {
    WorkflowDefinitionSaveCommand requiredCommand = WorkflowServiceValidation.requireObject(command,
        "工作流流程定义保存命令不能为空");
    WorkflowDefinition definition = new WorkflowDefinition();
    definition.setId(requiredCommand.getId());
    definition.setTenantId(requireText(requiredCommand.getTenantId(), "租户业务编码不能为空"));
    definition.setProcessKey(requireText(requiredCommand.getProcessKey(), "流程定义 key 不能为空"));
    definition.setProcessName(requireText(requiredCommand.getProcessName(), "流程定义名称不能为空"));
    definition.setVersion(requirePositiveVersion(requiredCommand.getVersion()));
    definition.setDeploymentId(requireText(requiredCommand.getDeploymentId(), "Flowable 部署 ID 不能为空"));
    definition.setStatus(normalizeStatus(defaultText(requiredCommand.getStatus(), DEFAULT_STATUS)));
    definition.setRemark(trimToNull(requiredCommand.getRemark()));
    definitionRepository.save(definition);
  }

  /**
   * 转换为响应对象。
   *
   * @param definition 工作流流程定义模型
   * @return 工作流流程定义响应对象
   */
  private WorkflowDefinitionResponse toResponse(WorkflowDefinition definition) {
    return new WorkflowDefinitionResponse(definition.getId(), definition.getTenantId(),
        definition.getProcessKey(), definition.getProcessName(), definition.getVersion(),
        definition.getDeploymentId(), definition.getStatus(), definition.getCreatedAt(),
        definition.getUpdatedAt(), definition.getRemark());
  }

  /**
   * 校验版本号必须为正整数。
   *
   * @param version 原始版本号
   * @return 版本号
   */
  private Integer requirePositiveVersion(Integer version) {
    if (version == null || version <= 0) {
      throw WorkflowServiceValidation.businessFailure("流程定义版本号必须大于 0");
    }
    return version;
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
   * 校验并规范化流程定义状态。
   *
   * @param status 流程定义状态编码
   * @return 规范化后的流程定义状态编码
   */
  private String normalizeStatus(String status) {
    try {
      return WorkflowDefinitionStatus.fromCode(status).getCode();
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
