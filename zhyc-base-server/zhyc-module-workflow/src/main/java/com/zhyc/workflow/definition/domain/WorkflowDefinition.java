/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition.domain;

import java.time.LocalDateTime;

/**
 * 工作流流程定义版本模型。
 *
 * <p>保存平台侧流程 key、版本和 Flowable 部署 ID 的对应关系，供表单绑定和业务发起流程使用。</p>
 */
public class WorkflowDefinition {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
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
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;
  /** 备注说明。 */
  private String remark;

  /** 创建空流程定义对象。 */
  public WorkflowDefinition() {
  }

  /**
   * 创建完整流程定义对象。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param processKey 流程定义 key
   * @param processName 流程定义名称
   * @param version 流程定义版本号
   * @param deploymentId Flowable 部署 ID
   * @param status 流程定义状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   * @param remark 备注说明
   */
  public WorkflowDefinition(Long id, String tenantId, String processKey, String processName,
      Integer version, String deploymentId, String status, LocalDateTime createdAt,
      LocalDateTime updatedAt, String remark) {
    this.id = id;
    this.tenantId = tenantId;
    this.processKey = processKey;
    this.processName = processName;
    this.version = version;
    this.deploymentId = deploymentId;
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
