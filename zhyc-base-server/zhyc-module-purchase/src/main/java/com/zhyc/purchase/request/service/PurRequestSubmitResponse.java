/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.service;

/**
 * 采购申请提交审批响应。
 */
public class PurRequestSubmitResponse {

  /** 采购申请单号。 */
  private final String requestNo;
  /** 工作流流程实例 ID。 */
  private final String processInstanceId;
  /** 提交后的流程状态。 */
  private final String processStatus;

  /**
   * 创建采购申请提交审批响应。
   *
   * @param requestNo 采购申请单号
   * @param processInstanceId 工作流流程实例 ID
   * @param processStatus 提交后的流程状态
   */
  public PurRequestSubmitResponse(String requestNo, String processInstanceId, String processStatus) {
    this.requestNo = requestNo;
    this.processInstanceId = processInstanceId;
    this.processStatus = processStatus;
  }

  public String getRequestNo() {
    return requestNo;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessStatus() {
    return processStatus;
  }
}
