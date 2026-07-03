/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.service;

import java.time.LocalDateTime;

/**
 * 工作流抄送任务列表项。
 */
public class WorkflowCcTaskItem {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 抄送记录 ID。 */
  private final Long ccRecordId;
  /** 流程实例 ID。 */
  private final String processInstanceId;
  /** 流程定义 key。 */
  private final String processKey;
  /** 业务对象唯一标识。 */
  private final String businessKey;
  /** 抄送接收人用户 ID。 */
  private final Long receiverId;
  /** 是否已读，0 未读，1 已读。 */
  private final Integer readFlag;
  /** 抄送创建时间。 */
  private final LocalDateTime createdAt;

  /**
   * 创建工作流抄送任务列表项。
   *
   * @param tenantId 租户业务编码
   * @param ccRecordId 抄送记录 ID
   * @param processInstanceId 流程实例 ID
   * @param processKey 流程定义 key
   * @param businessKey 业务对象唯一标识
   * @param receiverId 抄送接收人用户 ID
   * @param readFlag 是否已读
   * @param createdAt 抄送创建时间
   */
  public WorkflowCcTaskItem(String tenantId, Long ccRecordId, String processInstanceId,
      String processKey, String businessKey, Long receiverId, Integer readFlag,
      LocalDateTime createdAt) {
    this.tenantId = tenantId;
    this.ccRecordId = ccRecordId;
    this.processInstanceId = processInstanceId;
    this.processKey = processKey;
    this.businessKey = businessKey;
    this.receiverId = receiverId;
    this.readFlag = readFlag;
    this.createdAt = createdAt;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Long getCcRecordId() {
    return ccRecordId;
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

  public Long getReceiverId() {
    return receiverId;
  }

  public Integer getReadFlag() {
    return readFlag;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
