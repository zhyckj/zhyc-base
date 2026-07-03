/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model.service;

import java.time.LocalDateTime;

/**
 * 工作流流程模型响应对象。
 */
public class WorkflowProcessModelResponse {

  /** 流程模型主键。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 流程模型编码。 */
  private final String modelCode;
  /** 流程模型名称。 */
  private final String modelName;
  /** 流程分类 ID。 */
  private final Long categoryId;
  /** Flowable 模型 ID。 */
  private final String flowableModelId;
  /** BPMN XML 设计稿。 */
  private final String bpmnXml;
  /** 流程模型状态。 */
  private final String status;
  /** 创建时间。 */
  private final LocalDateTime createdAt;
  /** 更新时间。 */
  private final LocalDateTime updatedAt;
  /** 备注说明。 */
  private final String remark;

  /**
   * 创建工作流流程模型响应对象。
   *
   * @param id 流程模型主键
   * @param tenantId 租户业务编码
   * @param modelCode 流程模型编码
   * @param modelName 流程模型名称
   * @param categoryId 流程分类 ID
   * @param flowableModelId Flowable 模型 ID
   * @param bpmnXml BPMN XML 设计稿
   * @param status 流程模型状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   * @param remark 备注说明
   */
  public WorkflowProcessModelResponse(Long id, String tenantId, String modelCode,
      String modelName, Long categoryId, String flowableModelId, String bpmnXml, String status,
      LocalDateTime createdAt, LocalDateTime updatedAt, String remark) {
    this.id = id;
    this.tenantId = tenantId;
    this.modelCode = modelCode;
    this.modelName = modelName;
    this.categoryId = categoryId;
    this.flowableModelId = flowableModelId;
    this.bpmnXml = bpmnXml;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.remark = remark;
  }

  /** @return 流程模型主键 */
  public Long getId() {
    return id;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @return 流程模型编码 */
  public String getModelCode() {
    return modelCode;
  }

  /** @return 流程模型名称 */
  public String getModelName() {
    return modelName;
  }

  /** @return 流程分类 ID */
  public Long getCategoryId() {
    return categoryId;
  }

  /** @return Flowable 模型 ID */
  public String getFlowableModelId() {
    return flowableModelId;
  }

  /** @return BPMN XML 设计稿 */
  public String getBpmnXml() {
    return bpmnXml;
  }

  /** @return 流程模型状态 */
  public String getStatus() {
    return status;
  }

  /** @return 创建时间 */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /** @return 更新时间 */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /** @return 备注说明 */
  public String getRemark() {
    return remark;
  }
}
