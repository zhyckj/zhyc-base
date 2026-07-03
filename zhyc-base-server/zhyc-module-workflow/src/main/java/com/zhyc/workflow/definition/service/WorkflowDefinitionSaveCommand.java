/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition.service;

/**
 * 工作流流程定义保存命令。
 */
public class WorkflowDefinitionSaveCommand {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 流程定义主键。 */
  private final Long id;
  /** 流程定义 key。 */
  private final String processKey;
  /** 流程定义名称。 */
  private final String processName;
  /** 流程定义版本号。 */
  private final Integer version;
  /** Flowable 部署 ID。 */
  private final String deploymentId;
  /** 流程定义状态。 */
  private final String status;
  /** 备注说明。 */
  private final String remark;

  /**
   * 创建工作流流程定义保存命令。
   *
   * @param tenantId 租户业务编码
   * @param id 流程定义主键
   * @param processKey 流程定义 key
   * @param processName 流程定义名称
   * @param version 流程定义版本号
   * @param deploymentId Flowable 部署 ID
   * @param status 流程定义状态
   * @param remark 备注说明
   */
  public WorkflowDefinitionSaveCommand(String tenantId, Long id, String processKey,
      String processName, Integer version, String deploymentId, String status, String remark) {
    this.tenantId = tenantId;
    this.id = id;
    this.processKey = processKey;
    this.processName = processName;
    this.version = version;
    this.deploymentId = deploymentId;
    this.status = status;
    this.remark = remark;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @return 流程定义主键 */
  public Long getId() {
    return id;
  }

  /** @return 流程定义 key */
  public String getProcessKey() {
    return processKey;
  }

  /** @return 流程定义名称 */
  public String getProcessName() {
    return processName;
  }

  /** @return 流程定义版本号 */
  public Integer getVersion() {
    return version;
  }

  /** @return Flowable 部署 ID */
  public String getDeploymentId() {
    return deploymentId;
  }

  /** @return 流程定义状态 */
  public String getStatus() {
    return status;
  }

  /** @return 备注说明 */
  public String getRemark() {
    return remark;
  }
}
