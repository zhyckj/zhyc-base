/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model.controller;

/**
 * 工作流流程模型保存请求。
 */
public class WorkflowProcessModelSaveRequest {

  /** 流程模型主键。 */
  private Long id;
  /** 流程分类 ID。 */
  private Long categoryId;
  /** 流程模型编码。 */
  private String modelCode;
  /** 流程模型名称。 */
  private String modelName;
  /** Flowable 模型 ID。 */
  private String flowableModelId;
  /** BPMN XML 设计稿。 */
  private String bpmnXml;
  /** 流程模型状态。 */
  private String status;
  /** 备注说明。 */
  private String remark;

  /** @return 流程模型主键 */
  public Long getId() {
    return id;
  }

  /** @param id 流程模型主键 */
  public void setId(Long id) {
    this.id = id;
  }

  /** @return 流程分类 ID */
  public Long getCategoryId() {
    return categoryId;
  }

  /** @param categoryId 流程分类 ID */
  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
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

  /** @return 备注说明 */
  public String getRemark() {
    return remark;
  }

  /** @param remark 备注说明 */
  public void setRemark(String remark) {
    this.remark = remark;
  }
}
