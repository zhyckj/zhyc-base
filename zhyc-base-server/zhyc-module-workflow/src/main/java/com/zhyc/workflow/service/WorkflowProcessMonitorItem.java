/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.service;

import java.time.LocalDateTime;

/**
 * 流程监控列表项。
 *
 * <p>用于后台管理端按租户查看流程实例运行状态，不绑定当前发起人或处理人。</p>
 */
public class WorkflowProcessMonitorItem {

  /** 租户业务编码，用于共享表模式下的数据隔离。 */
  private final String tenantId;
  /** 流程实例 ID，用于定位 Flowable 和平台运行态记录。 */
  private final String processInstanceId;
  /** 流程定义 key，用于识别业务流程类型。 */
  private final String processKey;
  /** 业务对象唯一标识，用于跳转或定位业务单据。 */
  private final String businessKey;
  /** 流程发起人用户 ID，用于审计和运维排查。 */
  private final Long starterUserId;
  /** 流程实例状态，用于判断运行、通过、驳回或撤回。 */
  private final String status;
  /** 流程发起时间，用于按最近启动排序。 */
  private final LocalDateTime startedAt;
  /** 流程结束时间，运行中流程为空。 */
  private final LocalDateTime endedAt;

  /**
   * 创建流程监控列表项。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param processKey 流程定义 key
   * @param businessKey 业务对象唯一标识
   * @param starterUserId 流程发起人用户 ID
   * @param status 流程实例状态
   * @param startedAt 流程发起时间
   * @param endedAt 流程结束时间
   */
  public WorkflowProcessMonitorItem(String tenantId, String processInstanceId,
      String processKey, String businessKey, Long starterUserId, String status,
      LocalDateTime startedAt, LocalDateTime endedAt) {
    this.tenantId = tenantId;
    this.processInstanceId = processInstanceId;
    this.processKey = processKey;
    this.businessKey = businessKey;
    this.starterUserId = starterUserId;
    this.status = status;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @return 流程实例 ID */
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  /** @return 流程定义 key */
  public String getProcessKey() {
    return processKey;
  }

  /** @return 业务对象唯一标识 */
  public String getBusinessKey() {
    return businessKey;
  }

  /** @return 流程发起人用户 ID */
  public Long getStarterUserId() {
    return starterUserId;
  }

  /** @return 流程实例状态 */
  public String getStatus() {
    return status;
  }

  /** @return 流程发起时间 */
  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  /** @return 流程结束时间 */
  public LocalDateTime getEndedAt() {
    return endedAt;
  }
}
