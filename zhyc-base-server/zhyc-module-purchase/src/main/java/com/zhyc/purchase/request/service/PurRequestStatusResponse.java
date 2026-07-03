/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购申请状态开放 API 响应。
 */
public class PurRequestStatusResponse {

  /** 采购申请单号。 */
  private final String requestNo;
  /** 采购申请标题。 */
  private final String requestTitle;
  /** 流程状态。 */
  private final String processStatus;
  /** 采购申请总金额。 */
  private final BigDecimal totalAmount;
  /** 提交审批时间。 */
  private final LocalDateTime submittedAt;

  /**
   * 创建采购申请状态开放 API 响应。
   *
   * @param requestNo 采购申请单号
   * @param requestTitle 采购申请标题
   * @param processStatus 流程状态
   * @param totalAmount 采购申请总金额
   * @param submittedAt 提交审批时间
   */
  public PurRequestStatusResponse(String requestNo, String requestTitle, String processStatus,
      BigDecimal totalAmount, LocalDateTime submittedAt) {
    this.requestNo = requestNo;
    this.requestTitle = requestTitle;
    this.processStatus = processStatus;
    this.totalAmount = totalAmount;
    this.submittedAt = submittedAt;
  }

  public String getRequestNo() {
    return requestNo;
  }

  public String getRequestTitle() {
    return requestTitle;
  }

  public String getProcessStatus() {
    return processStatus;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public LocalDateTime getSubmittedAt() {
    return submittedAt;
  }
}
