/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model.domain;

import java.time.LocalDateTime;

/**
 * 工作流流程模型。
 *
 * <p>用于保存租户内可编辑的流程设计模型，并关联 Flowable 模型 ID，后续由部署动作生成流程定义版本。</p>
 */
public class WorkflowProcessModel {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 流程模型编码，租户内唯一。 */
  private String modelCode;
  /** 流程模型名称。 */
  private String modelName;
  /** 流程分类 ID。 */
  private Long categoryId;
  /** Flowable 模型 ID。 */
  private String flowableModelId;
  /** BPMN XML 设计稿，用于保存在线流程编排草稿。 */
  private String bpmnXml;
  /** 流程模型状态，例如 enabled、disabled。 */
  private String status;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;
  /** 备注说明。 */
  private String remark;

  /**
   * 创建空流程模型对象。
   */
  public WorkflowProcessModel() {
  }

  /**
   * 创建完整流程模型对象。
   *
   * @param id 数据库主键
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
  public WorkflowProcessModel(Long id, String tenantId, String modelCode, String modelName,
      Long categoryId, String flowableModelId, String bpmnXml, String status, LocalDateTime createdAt,
      LocalDateTime updatedAt, String remark) {
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

  /** @return 数据库主键 */
  public Long getId() {
    return id;
  }

  /** @param id 数据库主键 */
  public void setId(Long id) {
    this.id = id;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @param tenantId 租户业务编码 */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  /** @return 流程模型编码 */
  public String getModelCode() {
    return modelCode;
  }

  /** @param modelCode 流程模型编码 */
  public void setModelCode(String modelCode) {
    this.modelCode = modelCode;
  }

  /** @return 流程模型名称 */
  public String getModelName() {
    return modelName;
  }

  /** @param modelName 流程模型名称 */
  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  /** @return 流程分类 ID */
  public Long getCategoryId() {
    return categoryId;
  }

  /** @param categoryId 流程分类 ID */
  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  /** @return Flowable 模型 ID */
  public String getFlowableModelId() {
    return flowableModelId;
  }

  /** @param flowableModelId Flowable 模型 ID */
  public void setFlowableModelId(String flowableModelId) {
    this.flowableModelId = flowableModelId;
  }

  /** @return BPMN XML 设计稿 */
  public String getBpmnXml() {
    return bpmnXml;
  }

  /** @param bpmnXml BPMN XML 设计稿 */
  public void setBpmnXml(String bpmnXml) {
    this.bpmnXml = bpmnXml;
  }

  /** @return 流程模型状态 */
  public String getStatus() {
    return status;
  }

  /** @param status 流程模型状态 */
  public void setStatus(String status) {
    this.status = status;
  }

  /** @return 创建时间 */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /** @param createdAt 创建时间 */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /** @return 更新时间 */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /** @param updatedAt 更新时间 */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  /** @return 备注说明 */
  public String getRemark() {
    return remark;
  }

  /** @param remark 备注说明 */
  public void setRemark(String remark) {
    this.remark = remark;
  }
}
