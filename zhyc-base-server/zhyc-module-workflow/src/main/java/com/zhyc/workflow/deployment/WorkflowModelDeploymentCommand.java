/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.deployment;

/**
 * 工作流流程模型发布命令。
 *
 * <p>用于把平台流程模型发布为 Flowable 部署资源，并生成平台侧流程定义版本。</p>
 */
public class WorkflowModelDeploymentCommand {

  /** 租户业务编码，用于限制只能发布当前租户内流程模型。 */
  private final String tenantId;
  /** 平台流程模型主键。 */
  private final Long modelId;
  /** BPMN XML 文本，来源于流程设计器或模型转换器。 */
  private final String bpmnXml;
  /** 发布备注，写入平台流程定义版本。 */
  private final String remark;

  /**
   * 创建工作流流程模型发布命令。
   *
   * @param tenantId 租户业务编码
   * @param modelId 平台流程模型主键
   * @param bpmnXml BPMN XML 文本
   * @param remark 发布备注
   */
  public WorkflowModelDeploymentCommand(String tenantId, Long modelId, String bpmnXml,
      String remark) {
    this.tenantId = tenantId;
    this.modelId = modelId;
    this.bpmnXml = bpmnXml;
    this.remark = remark;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @return 平台流程模型主键 */
  public Long getModelId() {
    return modelId;
  }

  /** @return BPMN XML 文本 */
  public String getBpmnXml() {
    return bpmnXml;
  }

  /** @return 发布备注 */
  public String getRemark() {
    return remark;
  }
}
