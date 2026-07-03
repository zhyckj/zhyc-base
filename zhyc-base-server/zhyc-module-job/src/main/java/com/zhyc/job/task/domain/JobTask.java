/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.domain;

import java.time.LocalDateTime;

/**
 * 在线作业任务领域模型。
 *
 * <p>用于描述租户内可在线配置和手动触发的定时作业。</p>
 */
public class JobTask {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 作业任务编码。 */
  private String jobCode;
  /** 作业任务名称。 */
  private String jobName;
  /** Cron 表达式。 */
  private String cronExpression;
  /** 任务处理器名称。 */
  private String handlerName;
  /** 作业任务说明。 */
  private String jobDescription;
  /** 作业状态。 */
  private String status;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;

  /**
   * 创建空作业任务对象。
   */
  public JobTask() {
  }

  /**
   * 创建完整作业任务对象。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param jobCode 作业任务编码
   * @param jobName 作业任务名称
   * @param cronExpression Cron 表达式
   * @param handlerName 任务处理器名称
   * @param jobDescription 作业任务说明
   * @param status 作业状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public JobTask(Long id, String tenantId, String jobCode, String jobName, String cronExpression,
      String handlerName, String jobDescription, String status, LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.jobCode = jobCode;
    this.jobName = jobName;
    this.cronExpression = cronExpression;
    this.handlerName = handlerName;
    this.jobDescription = jobDescription;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
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

  /** @return 作业任务编码 */
  public String getJobCode() {
    return jobCode;
  }

  /** @param jobCode 作业任务编码 */
  public void setJobCode(String jobCode) {
    this.jobCode = jobCode;
  }

  /** @return 作业任务名称 */
  public String getJobName() {
    return jobName;
  }

  /** @param jobName 作业任务名称 */
  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  /** @return Cron 表达式 */
  public String getCronExpression() {
    return cronExpression;
  }

  /** @param cronExpression Cron 表达式 */
  public void setCronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
  }

  /** @return 任务处理器名称 */
  public String getHandlerName() {
    return handlerName;
  }

  /** @param handlerName 任务处理器名称 */
  public void setHandlerName(String handlerName) {
    this.handlerName = handlerName;
  }

  /** @return 作业任务说明 */
  public String getJobDescription() {
    return jobDescription;
  }

  /** @param jobDescription 作业任务说明 */
  public void setJobDescription(String jobDescription) {
    this.jobDescription = jobDescription;
  }

  /** @return 作业状态 */
  public String getStatus() {
    return status;
  }

  /** @param status 作业状态 */
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
}
