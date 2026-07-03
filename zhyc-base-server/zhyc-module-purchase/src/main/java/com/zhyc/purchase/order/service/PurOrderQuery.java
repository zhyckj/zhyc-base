/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.service;

/**
 * 采购订单分页查询条件。
 */
public class PurOrderQuery {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 订单状态，为空时查询全部状态。 */
  private final String orderStatus;
  /** 当前页码，从 1 开始。 */
  private final int pageNo;
  /** 每页记录数。 */
  private final int pageSize;

  /**
   * 创建采购订单分页查询条件。
   *
   * @param tenantId 租户业务编码
   * @param orderStatus 订单状态
   * @param pageNo 当前页码
   * @param pageSize 每页记录数
   */
  public PurOrderQuery(String tenantId, String orderStatus, int pageNo, int pageSize) {
    this.tenantId = tenantId;
    this.orderStatus = orderStatus;
    this.pageNo = pageNo;
    this.pageSize = pageSize;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getOrderStatus() {
    return orderStatus;
  }

  public int getPageNo() {
    return pageNo;
  }

  public int getPageSize() {
    return pageSize;
  }
}
