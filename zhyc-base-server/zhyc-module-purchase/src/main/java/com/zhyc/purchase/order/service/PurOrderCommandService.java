/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.service;

import com.zhyc.common.api.PageResult;

/**
 * 采购订单命令服务。
 */
public interface PurOrderCommandService {

  /**
   * 创建采购订单。
   *
   * @param command 采购订单创建命令
   * @return 采购订单号
   */
  String create(PurOrderCreateCommand command);

  /**
   * 查询采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @return 采购订单响应
   */
  PurOrderResponse get(String tenantId, String orderNo);

  /**
   * 分页查询采购订单。
   *
   * @param query 采购订单分页查询条件
   * @return 采购订单分页响应
   */
  PageResult<PurOrderResponse> list(PurOrderQuery query);

  /**
   * 确认采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @return 采购订单响应
   */
  PurOrderResponse confirm(String tenantId, String orderNo);

  /**
   * 关闭采购订单。
   *
   * @param tenantId 租户业务编码
   * @param orderNo 采购订单号
   * @return 采购订单响应
   */
  PurOrderResponse close(String tenantId, String orderNo);
}
