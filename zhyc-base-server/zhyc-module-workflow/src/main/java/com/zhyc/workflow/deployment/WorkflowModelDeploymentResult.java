/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.deployment;

/**
 * 工作流流程模型发布结果。
 *
 * <p>返回平台流程定义版本和 Flowable 部署 ID，供后台管理端发布结果提示和后续审计使用。</p>
 */
public class WorkflowModelDeploymentResult {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 流程定义 key，来自平台流程模型编码。 */
  private final String processKey;
  /** 流程定义名称，来自平台流程模型名称。 */
  private final String processName;
  /** 平台侧流程定义版本号。 */
  private final Integer version;
  /** Flowable 部署 ID。 */
  private final String deploymentId;

  /**
   * 创建工作流流程模型发布结果。
   *
   * @param tenantId 租户业务编码
   * @param processKey 流程定义 key
   * @param processName 流程定义名称
   * @param version 平台侧流程定义版本号
   * @param deploymentId Flowable 部署 ID
   */
  public WorkflowModelDeploymentResult(String tenantId, String processKey, String processName,
      Integer version, String deploymentId) {
    this.tenantId = tenantId;
    this.processKey = processKey;
    this.processName = processName;
    this.version = version;
    this.deploymentId = deploymentId;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @return 流程定义 key */
  public String getProcessKey() {
    return processKey;
  }

  /** @return 流程定义名称 */
  public String getProcessName() {
    return processName;
  }

  /** @return 平台侧流程定义版本号 */
  public Integer getVersion() {
    return version;
  }

  /** @return Flowable 部署 ID */
  public String getDeploymentId() {
    return deploymentId;
  }
}
