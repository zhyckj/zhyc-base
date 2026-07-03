/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.domain;

import java.time.LocalDateTime;

/**
 * 全文检索索引重建任务。
 *
 * <p>记录索引重建请求和执行状态，首期用于后台运维编排，后续可由调度中心或消息队列消费。</p>
 */
public class SearchRebuildTask {

  /** 重建任务主键。 */
  private final Long id;
  /** 租户业务编码，用于隔离不同租户的重建任务。 */
  private final String tenantId;
  /** 待重建索引编码。 */
  private final String indexCode;
  /** 任务状态，pending 表示待执行。 */
  private final String taskStatus;
  /** 触发类型，manual 表示后台人工触发。 */
  private final String triggerType;
  /** 任务开始时间。 */
  private final LocalDateTime startedAt;
  /** 任务完成时间。 */
  private final LocalDateTime finishedAt;
  /** 失败错误信息，禁止记录敏感明文。 */
  private final String errorMessage;
  /** 创建时间。 */
  private final LocalDateTime createdAt;
  /** 更新时间。 */
  private final LocalDateTime updatedAt;

  /**
   * 创建索引重建任务。
   *
   * @param id 重建任务主键
   * @param tenantId 租户业务编码
   * @param indexCode 待重建索引编码
   * @param taskStatus 任务状态
   * @param triggerType 触发类型
   * @param startedAt 任务开始时间
   * @param finishedAt 任务完成时间
   * @param errorMessage 失败错误信息
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public SearchRebuildTask(Long id, String tenantId, String indexCode, String taskStatus, String triggerType,
      LocalDateTime startedAt, LocalDateTime finishedAt, String errorMessage,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.indexCode = indexCode;
    this.taskStatus = taskStatus;
    this.triggerType = triggerType;
    this.startedAt = startedAt;
    this.finishedAt = finishedAt;
    this.errorMessage = errorMessage;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getIndexCode() {
    return indexCode;
  }

  public String getTaskStatus() {
    return taskStatus;
  }

  public String getTriggerType() {
    return triggerType;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public LocalDateTime getFinishedAt() {
    return finishedAt;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
