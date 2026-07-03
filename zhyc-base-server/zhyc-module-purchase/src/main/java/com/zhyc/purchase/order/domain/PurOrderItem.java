/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.domain;

import java.math.BigDecimal;

/**
 * 采购订单明细领域对象。
 */
public class PurOrderItem {

  /** 物品名称。 */
  private final String itemName;
  /** 采购数量。 */
  private final BigDecimal quantity;
  /** 采购单价。 */
  private final BigDecimal unitPrice;
  /** 明细金额。 */
  private final BigDecimal amount;

  /**
   * 创建采购订单明细领域对象。
   *
   * @param itemName 物品名称
   * @param quantity 采购数量
   * @param unitPrice 采购单价
   * @param amount 明细金额
   */
  public PurOrderItem(String itemName, BigDecimal quantity, BigDecimal unitPrice,
      BigDecimal amount) {
    this.itemName = itemName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.amount = amount;
  }

  public String getItemName() {
    return itemName;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public BigDecimal getAmount() {
    return amount;
  }
}
