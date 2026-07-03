/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购订单领域对象。
 */
public class PurOrder {

  /** 租户业务编码。 */
  private final String tenantId;
  /** 采购订单号。 */
  private final String orderNo;
  /** 采购申请单号。 */
  private final String requestNo;
  /** 供应商 ID。 */
  private final Long supplierId;
  /** 采购员用户 ID。 */
  private final Long buyerId;
  /** 采购订单总金额。 */
  private final BigDecimal totalAmount;
  /** 订单状态。 */
  private final String orderStatus;
  /** 采购订单明细。 */
  private final List<PurOrderItem> items;

  /**
   * 创建采购订单领域对象。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @param requestNo 采购申请单号
   * @param supplierId 供应商 ID
   * @param buyerId 采购员用户 ID
   * @param totalAmount 采购订单总金额
   * @param orderStatus 订单状态
   * @param items 采购订单明细
   */
  public PurOrder(String tenantId, String orderNo, String requestNo, Long supplierId,
      Long buyerId, BigDecimal totalAmount, String orderStatus, List<PurOrderItem> items) {
    this.tenantId = tenantId;
    this.orderNo = orderNo;
    this.requestNo = requestNo;
    this.supplierId = supplierId;
    this.buyerId = buyerId;
    this.totalAmount = totalAmount;
    this.orderStatus = orderStatus;
    this.items = items == null ? List.of() : List.copyOf(items);
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getOrderNo() {
    return orderNo;
  }

  public String getRequestNo() {
    return requestNo;
  }

  public Long getSupplierId() {
    return supplierId;
  }

  public Long getBuyerId() {
    return buyerId;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public String getOrderStatus() {
    return orderStatus;
  }

  public List<PurOrderItem> getItems() {
    return items;
  }
}
