/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.repository;

import com.zhyc.purchase.order.domain.PurOrder;
import java.util.List;
import java.util.Optional;

/**
 * 采购订单仓储。
 */
public interface PurOrderRepository {

  /**
   * 保存采购订单。
   *
   * @param purOrder 采购订单领域对象
   */
  void save(PurOrder purOrder);

  /**
   * 按租户和采购订单号查询采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @return 采购订单
   */
  Optional<PurOrder> findByTenantIdAndOrderNo(String tenantId, String orderNo);

  /**
   * 按租户和订单状态统计采购订单数量。
   *
   * @param tenantId 租户业务编码
   * @param orderStatus 订单状态，空表示全部状态
   * @return 采购订单数量
   */
  long countByTenantIdAndStatus(String tenantId, String orderStatus);

  /**
   * 按租户和订单状态分页查询采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderStatus 订单状态，空表示全部状态
   * @param offset 起始偏移量
   * @param pageSize 每页记录数
   * @return 采购订单列表
   */
  List<PurOrder> findPageByTenantIdAndStatus(String tenantId, String orderStatus,
      long offset, int pageSize);

  /**
   * 更新采购订单状态。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @param orderStatus 订单状态
   */
  void updateStatus(String tenantId, String orderNo, String orderStatus);
}
