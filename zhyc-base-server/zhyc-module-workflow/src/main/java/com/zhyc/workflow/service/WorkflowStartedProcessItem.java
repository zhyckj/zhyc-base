/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.service;

import java.time.LocalDateTime;

/**
 * 当前用户发起的流程列表项。
 */
public class WorkflowStartedProcessItem {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 流程实例 ID。 */
  private final String processInstanceId;
  /** 流程定义 key。 */
  private final String processKey;
  /** 业务对象唯一标识。 */
  private final String businessKey;
  /** 流程发起人用户 ID。 */
  private final Long starterUserId;
  /** 流程实例状态。 */
  private final String status;
  /** 流程发起时间。 */
  private final LocalDateTime startedAt;

  /**
   * 创建当前用户发起的流程列表项。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param processKey 流程定义 key
   * @param businessKey 业务对象唯一标识
   * @param starterUserId 流程发起人用户 ID
   * @param status 流程实例状态
   * @param startedAt 流程发起时间
   */
  public WorkflowStartedProcessItem(String tenantId, String processInstanceId, String processKey,
      String businessKey, Long starterUserId, String status, LocalDateTime startedAt) {
    this.tenantId = tenantId;
    this.processInstanceId = processInstanceId;
    this.processKey = processKey;
    this.businessKey = businessKey;
    this.starterUserId = starterUserId;
    this.status = status;
    this.startedAt = startedAt;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessKey() {
    return processKey;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public Long getStarterUserId() {
    return starterUserId;
  }

  public String getStatus() {
    return status;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }
}
