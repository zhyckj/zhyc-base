/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition.controller;

/**
 * 工作流流程定义保存请求。
 */
public class WorkflowDefinitionSaveRequest {

  /** 流程定义主键。 */
  private Long id;
  /** 流程定义 key。 */
  private String processKey;
  /** 流程定义名称。 */
  private String processName;
  /** 流程定义版本号。 */
  private Integer version;
  /** Flowable 部署 ID。 */
  private String deploymentId;
  /** 流程定义状态。 */
  private String status;
  /** 备注说明。 */
  private String remark;

  /** @return 流程定义主键 */
  public Long getId() {
    return id;
  }

  /** @param id 流程定义主键 */
  public void setId(Long id) {
    this.id = id;
  }

  /** @return 流程定义 key */
  public String getProcessKey() {
    return processKey;
  }

  /** @param processKey 流程定义 key */
  public void setProcessKey(String processKey) {
    this.processKey = processKey;
  }

  /** @return 流程定义名称 */
  public String getProcessName() {
    return processName;
  }

  /** @param processName 流程定义名称 */
  public void setProcessName(String processName) {
    this.processName = processName;
  }

  /** @return 流程定义版本号 */
  public Integer getVersion() {
    return version;
  }

  /** @param version 流程定义版本号 */
  public void setVersion(Integer version) {
    this.version = version;
  }

  /** @return Flowable 部署 ID */
  public String getDeploymentId() {
    return deploymentId;
  }

  /** @param deploymentId Flowable 部署 ID */
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  /** @return 流程定义状态 */
  public String getStatus() {
    return status;
  }

  /** @param status 流程定义状态 */
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
