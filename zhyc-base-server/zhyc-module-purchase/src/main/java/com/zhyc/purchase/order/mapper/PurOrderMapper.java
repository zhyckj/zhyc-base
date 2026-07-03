/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.mapper;

import com.zhyc.purchase.order.domain.PurOrder;
import com.zhyc.purchase.order.domain.PurOrderItem;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 采购订单 MyBatis Mapper。
 */
@Mapper
public interface PurOrderMapper {

  /**
   * 写入采购订单主表。
   *
   * @param purOrder 采购订单领域对象
   */
  @InsertProvider(type = PurOrderSqlProvider.class, method = "insertOrder")
  void insertOrder(PurOrder purOrder);

  /**
   * 写入采购订单明细。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @param item 采购订单明细
   */
  @InsertProvider(type = PurOrderSqlProvider.class, method = "insertItem")
  void insertItem(@Param("tenantId") String tenantId, @Param("orderNo") String orderNo,
                  @Param("item") PurOrderItem item);

  /**
   * 按租户和订单号查询采购订单主表。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @return 采购订单主表记录
   */
  @SelectProvider(type = PurOrderSqlProvider.class, method = "selectOrderByTenantIdAndOrderNo")
  PurOrderRecord selectOrderByTenantIdAndOrderNo(@Param("tenantId") String tenantId,
                                                 @Param("orderNo") String orderNo);

  /**
   * 按租户和订单号查询采购订单明细。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @return 采购订单明细
   */
  @SelectProvider(type = PurOrderSqlProvider.class, method = "selectItemsByTenantIdAndOrderNo")
  List<PurOrderItem> selectItemsByTenantIdAndOrderNo(@Param("tenantId") String tenantId,
                                                     @Param("orderNo") String orderNo);

  /**
   * 按租户和订单状态统计采购订单数量。
   *
   * @param tenantId 租户业务编码
   * @param orderStatus 订单状态
   * @return 采购订单数量
   */
  @SelectProvider(type = PurOrderSqlProvider.class, method = "countByTenantIdAndStatus")
  long countByTenantIdAndStatus(@Param("tenantId") String tenantId,
                                @Param("orderStatus") String orderStatus);

  /**
   * 按租户和订单状态分页查询采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderStatus 订单状态
   * @param offset 起始偏移量
   * @param pageSize 每页记录数
   * @return 采购订单主表记录
   */
  @SelectProvider(type = PurOrderSqlProvider.class, method = "selectPageByTenantIdAndStatus")
  List<PurOrderRecord> selectPageByTenantIdAndStatus(@Param("tenantId") String tenantId,
                                                     @Param("orderStatus") String orderStatus,
                                                     @Param("offset") long offset,
                                                     @Param("pageSize") int pageSize);

  /**
   * 更新采购订单状态。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @param orderStatus 订单状态
   */
  @UpdateProvider(type = PurOrderSqlProvider.class, method = "updateStatus")
  void updateStatus(@Param("tenantId") String tenantId, @Param("orderNo") String orderNo,
                    @Param("orderStatus") String orderStatus);
}
