/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.service;

/**
 * 采购申请分页查询条件。
 */
public class PurRequestQuery {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 流程状态，为空时查询全部状态。 */
  private final String processStatus;
  /** 当前页码，从 1 开始。 */
  private final int pageNo;
  /** 每页记录数。 */
  private final int pageSize;

  /**
   * 创建采购申请分页查询条件。
   *
   * @param tenantId 租户业务编码
   * @param processStatus 流程状态
   * @param pageNo 当前页码
   * @param pageSize 每页记录数
   */
  public PurRequestQuery(String tenantId, String processStatus, int pageNo, int pageSize) {
    this.tenantId = tenantId;
    this.processStatus = processStatus;
    this.pageNo = pageNo;
    this.pageSize = pageSize;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getProcessStatus() {
    return processStatus;
  }

  public int getPageNo() {
    return pageNo;
  }

  public int getPageSize() {
    return pageSize;
  }
}
