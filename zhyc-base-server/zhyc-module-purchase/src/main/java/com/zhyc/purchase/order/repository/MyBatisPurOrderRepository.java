/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.repository;

import com.zhyc.purchase.order.domain.PurOrder;
import com.zhyc.purchase.order.mapper.PurOrderMapper;
import com.zhyc.purchase.order.mapper.PurOrderRecord;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的采购订单仓储实现。
 */
@Repository
public class MyBatisPurOrderRepository implements PurOrderRepository {

  /** 采购订单 Mapper。 */
  private final PurOrderMapper purOrderMapper;

  /**
   * 创建 MyBatis 采购订单仓储。
   *
   * @param purOrderMapper 采购订单 Mapper
   */
  public MyBatisPurOrderRepository(PurOrderMapper purOrderMapper) {
    this.purOrderMapper = Objects.requireNonNull(purOrderMapper, "采购订单 Mapper 不能为空");
  }

  @Override
  public void save(PurOrder purOrder) {
    purOrderMapper.insertOrder(purOrder);
    for (var item : purOrder.getItems()) {
      purOrderMapper.insertItem(purOrder.getTenantId(), purOrder.getOrderNo(), item);
    }
  }

  @Override
  public Optional<PurOrder> findByTenantIdAndOrderNo(String tenantId, String orderNo) {
    PurOrderRecord record = purOrderMapper.selectOrderByTenantIdAndOrderNo(tenantId, orderNo);
    if (record == null) {
      return Optional.empty();
    }
    return Optional.of(record.toDomain(purOrderMapper.selectItemsByTenantIdAndOrderNo(tenantId, orderNo)));
  }

  @Override
  public long countByTenantIdAndStatus(String tenantId, String orderStatus) {
    return purOrderMapper.countByTenantIdAndStatus(tenantId, orderStatus);
  }

  @Override
  public List<PurOrder> findPageByTenantIdAndStatus(String tenantId, String orderStatus,
      long offset, int pageSize) {
    return purOrderMapper.selectPageByTenantIdAndStatus(tenantId, orderStatus, offset, pageSize)
        .stream()
        .map(record -> record.toDomain(List.of()))
        .toList();
  }

  @Override
  public void updateStatus(String tenantId, String orderNo, String orderStatus) {
    purOrderMapper.updateStatus(tenantId, orderNo, orderStatus);
  }
}
