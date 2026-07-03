/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.service;

import java.math.BigDecimal;

/**
 * 采购申请创建命令。
 */
public class PurRequestCreateCommand {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 采购申请单号。 */
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

  /**
   * 创建采购申请创建命令。
   *
   * @param tenantId 租户业务编码
   * @param requestNo 采购申请单号
   * @param requestTitle 采购申请标题
   * @param applicantId 申请人用户 ID
   * @param orgId 申请部门 ID
   * @param totalAmount 采购申请总金额
   * @param requestReason 采购申请原因
   */
  public PurRequestCreateCommand(String tenantId, String requestNo, String requestTitle,
      Long applicantId, Long orgId, BigDecimal totalAmount, String requestReason) {
    this.tenantId = tenantId;
    this.requestNo = requestNo;
    this.requestTitle = requestTitle;
    this.applicantId = applicantId;
    this.orgId = orgId;
    this.totalAmount = totalAmount;
    this.requestReason = requestReason;
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
}
