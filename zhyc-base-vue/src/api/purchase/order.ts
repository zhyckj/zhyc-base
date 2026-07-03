/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request, type PageResult } from '@/api/http';

/**
 * 采购订单明细创建参数。
 */
export interface PurOrderItemCommand {
  /** 物品名称。 */
  itemName: string;
  /** 采购数量。 */
  quantity: number;
  /** 采购单价。 */
  unitPrice: number;
  /** 明细金额。 */
  amount: number;
}

/**
 * 采购订单创建参数。
 */
export interface PurOrderCreateCommand {
  /** 租户编码。 */
  tenantId: string;
  /** 采购订单号。 */
  orderNo: string;
  /** 采购申请单号。 */
  requestNo: string;
  /** 供应商 ID。 */
  supplierId: number;
  /** 采购员用户 ID。 */
  buyerId: number;
  /** 采购订单总金额。 */
  totalAmount: number;
  /** 采购订单明细。 */
  items: PurOrderItemCommand[];
}

/**
 * 采购订单明细响应。
 */
export interface PurOrderItemResponse extends PurOrderItemCommand {}

/**
 * 采购订单响应。
 */
export interface PurOrderResponse {
  /** 租户编码。 */
  tenantId: string;
  /** 采购订单号。 */
  orderNo: string;
  /** 采购申请单号。 */
  requestNo: string;
  /** 供应商 ID。 */
  supplierId: number;
  /** 采购员用户 ID。 */
  buyerId: number;
  /** 采购订单总金额。 */
  totalAmount: number;
  /** 订单状态。 */
  orderStatus: string;
  /** 采购订单明细。 */
  items: PurOrderItemResponse[];
}

/**
 * 采购订单列表查询参数。
 */
export interface PurOrderListQuery {
  /** 租户编码。 */
  tenantId: string;
  /** 订单状态。 */
  orderStatus?: string;
  /** 当前页码。 */
  pageNo: number;
  /** 每页记录数。 */
  pageSize: number;
}

/** 采购订单分页响应。 */
export type PurOrderPageResponse = PageResult<PurOrderResponse>;

/**
 * 分页查询采购订单。
 */
export function listPurchaseOrders(query: PurOrderListQuery): Promise<PurOrderPageResponse> {
  return request<PurOrderPageResponse>('/purchase/orders', {
    query: {
      orderStatus: query.orderStatus,
      pageNo: query.pageNo,
      pageSize: query.pageSize,
    },
  });
}

/**
 * 创建采购订单。
 */
export function createPurchaseOrder(command: PurOrderCreateCommand): Promise<string> {
  return request<string, PurOrderCreateCommand>('/purchase/orders', {
    method: 'POST',
    body: command,
  });
}

/**
 * 查询采购订单。
 */
export function getPurchaseOrder(orderNo: string): Promise<PurOrderResponse> {
  return request<PurOrderResponse>(`/purchase/orders/${orderNo}`);
}

/**
 * 确认采购订单。
 */
export function confirmPurchaseOrder(orderNo: string): Promise<PurOrderResponse> {
  return request<PurOrderResponse>(`/purchase/orders/${orderNo}/confirm`, {
    method: 'POST',
  });
}

/**
 * 关闭采购订单。
 */
export function closePurchaseOrder(orderNo: string): Promise<PurOrderResponse> {
  return request<PurOrderResponse>(`/purchase/orders/${orderNo}/close`, {
    method: 'POST',
  });
}
