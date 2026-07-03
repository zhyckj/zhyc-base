/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购申请领域对象。
 *
 * <p>对应租户内采购申请主表 {@code pur_request}，当前首期先承载开放 API 查询申请状态所需字段。</p>
 */
public class PurRequest {

  /** 主键 ID。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 采购申请单号，租户内唯一。 */
  private final String requestNo;
  /** 采购申请标题。 */
  private final String requestTitle;
  /** 申请人用户 ID。 */
  private final Long applicantId;
  /** 申请部门 ID。 */
  private final Long orgId;
  /** 采购申请总金额。 */
  private final BigDecimal totalAmount;
  /** 采购申请原因。 */
  private final String requestReason;
  /** 流程状态，例如 DRAFT、APPROVING、APPROVED、REJECTED、WITHDRAWN。 */
  private final String processStatus;
  /** 工作流流程实例 ID，草稿未提交时为空。 */
  private final String processInstanceId;
  /** 提交审批时间。 */
  private final LocalDateTime submittedAt;
  /** 创建时间。 */
  private final LocalDateTime createdAt;
  /** 更新时间。 */
  private final LocalDateTime updatedAt;

  /**
   * 创建采购申请领域对象。
   *
   * @param id 主键 ID
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @param requestTitle 采购申请标题
   * @param applicantId 申请人用户 ID
   * @param orgId 申请部门 ID
   * @param totalAmount 采购申请总金额
   * @param requestReason 采购申请原因
   * @param processStatus 流程状态
   * @param submittedAt 提交审批时间
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public PurRequest(Long id, String tenantId, String requestNo, String requestTitle, Long applicantId,
      Long orgId, BigDecimal totalAmount, String requestReason, String processStatus,
      LocalDateTime submittedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this(id, tenantId, requestNo, requestTitle, applicantId, orgId, totalAmount, requestReason,
        processStatus, null, submittedAt, createdAt, updatedAt);
  }

  /**
   * 创建采购申请领域对象。
   *
   * @param id 主键 ID
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @param requestTitle 采购申请标题
   * @param applicantId 申请人用户 ID
   * @param orgId 申请部门 ID
   * @param totalAmount 采购申请总金额
   * @param requestReason 采购申请原因
   * @param processStatus 流程状态
   * @param processInstanceId 工作流流程实例 ID
   * @param submittedAt 提交审批时间
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public PurRequest(Long id, String tenantId, String requestNo, String requestTitle, Long applicantId,
      Long orgId, BigDecimal totalAmount, String requestReason, String processStatus,
      String processInstanceId, LocalDateTime submittedAt, LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.requestNo = requestNo;
    this.requestTitle = requestTitle;
    this.applicantId = applicantId;
    this.orgId = orgId;
    this.totalAmount = totalAmount;
    this.requestReason = requestReason;
    this.processStatus = processStatus;
    this.processInstanceId = processInstanceId;
    this.submittedAt = submittedAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getRequestNo() {
    return requestNo;
  }

  public String getRequestTitle() {
    return requestTitle;
  }

  public Long getApplicantId() {
    return applicantId;
  }

  public Long getOrgId() {
    return orgId;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public String getRequestReason() {
    return requestReason;
  }

  public String getProcessStatus() {
    return processStatus;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public LocalDateTime getSubmittedAt() {
    return submittedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
