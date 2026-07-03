/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.deployment;

import com.zhyc.workflow.definition.domain.WorkflowDefinition;
import com.zhyc.workflow.definition.repository.WorkflowDefinitionRepository;
import com.zhyc.workflow.model.domain.WorkflowProcessModel;
import com.zhyc.workflow.model.repository.WorkflowProcessModelRepository;
import com.zhyc.workflow.support.WorkflowServiceValidation;
import java.util.Comparator;
import java.util.Objects;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认工作流流程模型发布服务实现。
 *
 * <p>负责把平台流程模型发布到 Flowable，并在平台侧生成可查询、可绑定的流程定义版本。</p>
 */
@Service
@ConditionalOnBean(RepositoryService.class)
public class DefaultWorkflowModelDeploymentService implements WorkflowModelDeploymentService {

  /** 默认流程定义启用状态。 */
  private static final String ACTIVE_STATUS = "active";
  /** 可发布的流程模型状态。 */
  private static final String ENABLED_STATUS = "enabled";
  /** BPMN 资源文件后缀。 */
  private static final String BPMN_RESOURCE_SUFFIX = ".bpmn20.xml";

  /** 工作流流程模型仓储。 */
  private final WorkflowProcessModelRepository modelRepository;
  /** 工作流流程定义仓储。 */
  private final WorkflowDefinitionRepository definitionRepository;
  /** Flowable 仓储服务，用于部署 BPMN 资源。 */
  private final RepositoryService repositoryService;

  /**
   * 创建默认工作流流程模型发布服务。
   *
   * @param modelRepository 工作流流程模型仓储
   * @param definitionRepository 工作流流程定义仓储
   * @param repositoryService Flowable 仓储服务
   */
  public DefaultWorkflowModelDeploymentService(WorkflowProcessModelRepository modelRepository,
      WorkflowDefinitionRepository definitionRepository, RepositoryService repositoryService) {
    this.modelRepository = Objects.requireNonNull(modelRepository, "工作流流程模型仓储不能为空");
    this.definitionRepository = Objects.requireNonNull(definitionRepository,
        "工作流流程定义仓储不能为空");
    this.repositoryService = Objects.requireNonNull(repositoryService, "Flowable 仓储服务不能为空");
  }

  /**
   * 发布工作流流程模型。
   *
   * <p>发布时会校验租户内模型归属和启用状态，将 BPMN XML 部署到 Flowable，并写入平台流程定义版本。</p>
   *
   * @param command 流程模型发布命令
   * @return 流程模型发布结果
   */
  @Override
  @Transactional
  public WorkflowModelDeploymentResult deploy(WorkflowModelDeploymentCommand command) {
    WorkflowModelDeploymentCommand requiredCommand = WorkflowServiceValidation.requireObject(command,
        "流程模型发布命令不能为空");
    String tenantId = requireText(requiredCommand.getTenantId(), "租户业务编码不能为空");
    Long modelId = requireModelId(requiredCommand.getModelId());
    String bpmnXml = requireText(requiredCommand.getBpmnXml(), "BPMN XML 不能为空");
    WorkflowProcessModel model = findTenantModel(tenantId, modelId);
    assertModelEnabled(model);
    String processKey = requireText(model.getModelCode(), "流程模型编码不能为空");
    String processName = requireText(model.getModelName(), "流程模型名称不能为空");
    Integer nextVersion = nextDefinitionVersion(tenantId, processKey);
    Deployment deployment = repositoryService.createDeployment()
        .tenantId(tenantId)
        .key(processKey)
        .name(processName)
        .addString(processKey + BPMN_RESOURCE_SUFFIX, bpmnXml)
        .deploy();
    String deploymentId = requireText(deployment.getId(), "Flowable 部署 ID 不能为空");
    definitionRepository.save(new WorkflowDefinition(null, tenantId, processKey, processName,
        nextVersion, deploymentId, ACTIVE_STATUS, null, null, trimToNull(requiredCommand.getRemark())));
    return new WorkflowModelDeploymentResult(tenantId, processKey, processName, nextVersion,
        deploymentId);
  }

  /**
   * 查找租户内流程模型。
   *
   * @param tenantId 租户业务编码
   * @param modelId 流程模型主键
   * @return 流程模型
   */
  private WorkflowProcessModel findTenantModel(String tenantId, Long modelId) {
    return modelRepository.findByTenantId(tenantId).stream()
        .filter(model -> modelId.equals(model.getId()))
        .findFirst()
        .orElseThrow(() -> WorkflowServiceValidation.businessFailure("流程模型不存在或不属于当前租户"));
  }

  /**
   * 校验流程模型必须处于启用状态。
   *
   * @param model 流程模型
   */
  private void assertModelEnabled(WorkflowProcessModel model) {
    if (!ENABLED_STATUS.equals(model.getStatus())) {
      throw WorkflowServiceValidation.businessFailure("流程模型未启用，不能发布");
    }
  }

  /**
   * 计算平台侧下一流程定义版本号。
   *
   * @param tenantId 租户业务编码
   * @param processKey 流程定义 key
   * @return 下一版本号
   */
  private Integer nextDefinitionVersion(String tenantId, String processKey) {
    return definitionRepository.findByTenantId(tenantId).stream()
        .filter(definition -> processKey.equals(definition.getProcessKey()))
        .map(WorkflowDefinition::getVersion)
        .filter(Objects::nonNull)
        .max(Comparator.naturalOrder())
        .map(version -> version + 1)
        .orElse(1);
  }

  /**
   * 校验流程模型主键不能为空。
   *
   * @param modelId 原始流程模型主键
   * @return 流程模型主键
   */
  private Long requireModelId(Long modelId) {
    if (modelId == null) {
      throw WorkflowServiceValidation.businessFailure("流程模型主键不能为空");
    }
    return modelId;
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
