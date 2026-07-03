/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { mobileRequest, type MobilePageResult } from './request';

/**
 * 移动端采购申请创建参数。
 */
export interface MobilePurchaseRequestCommand {
  /** 租户编码。 */
  tenantId: string;
  /** 采购申请单号。 */
  requestNo: string;
  /** 采购申请标题。 */
  requestTitle: string;
  /** 申请人用户 ID。 */
  applicantId: number;
  /** 申请部门 ID。 */
  orgId: number;
  /** 采购申请总金额。 */
  totalAmount: number;
  /** 采购申请原因。 */
  requestReason: string;
}

/**
 * 移动端采购申请提交结果。
 */
export interface MobilePurchaseSubmitResult {
  /** 采购申请单号。 */
  requestNo: string;
  /** 流程实例 ID。 */
  processInstanceId: string;
  /** 流程状态。 */
  processStatus: string;
}

/**
 * 移动端采购申请列表记录。
 */
export interface MobilePurchaseRequest {
  /** 采购申请单号。 */
  requestNo: string;
  /** 采购申请标题。 */
  requestTitle: string;
  /** 流程状态。 */
  processStatus: string;
  /** 采购申请总金额。 */
  totalAmount: number;
  /** 提交审批时间。 */
  submittedAt?: string;
}

/**
 * 移动端采购申请列表查询参数。
 */
export interface MobilePurchaseRequestQuery {
  /** 流程状态。 */
  processStatus?: string;
  /** 当前页码。 */
  pageNo: number;
  /** 每页记录数。 */
  pageSize: number;
}

/** 移动端采购申请分页响应。 */
export type MobilePurchaseRequestPage = MobilePageResult<MobilePurchaseRequest>;

/**
 * 移动端采购订单明细。
 */
export interface MobilePurchaseOrderItem {
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
 * 移动端采购订单。
 */
export interface MobilePurchaseOrder {
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
  items: MobilePurchaseOrderItem[];
}

/**
 * 移动端采购订单列表查询参数。
 */
export interface MobilePurchaseOrderQuery {
  /** 订单状态。 */
  orderStatus?: string;
  /** 当前页码。 */
  pageNo: number;
  /** 每页记录数。 */
  pageSize: number;
}

/** 移动端采购订单分页响应。 */
export type MobilePurchaseOrderPage = MobilePageResult<MobilePurchaseOrder>;

/**
 * 创建采购申请。
 */
export function createMobilePurchaseRequest(command: MobilePurchaseRequestCommand): Promise<string> {
  return mobileRequest<string, MobilePurchaseRequestCommand>('/purchase/requests', {
    method: 'POST',
    data: command,
  });
}

/**
 * 提交采购申请进入审批。
 */
export function submitMobilePurchaseRequest(
  requestNo: string,
): Promise<MobilePurchaseSubmitResult> {
  return mobileRequest<MobilePurchaseSubmitResult>(`/purchase/requests/${encodeURIComponent(requestNo)}/submit`, {
    method: 'POST',
  });
}

/**
 * 分页查询移动端采购申请。
 */
export function listMobilePurchaseRequests(query: MobilePurchaseRequestQuery): Promise<MobilePurchaseRequestPage> {
  return mobileRequest<MobilePurchaseRequestPage>('/purchase/requests', {
    query: {
      processStatus: query.processStatus,
      pageNo: query.pageNo,
      pageSize: query.pageSize,
    },
  });
}

/**
 * 分页查询移动端采购订单。
 */
export function listMobilePurchaseOrders(query: MobilePurchaseOrderQuery): Promise<MobilePurchaseOrderPage> {
  return mobileRequest<MobilePurchaseOrderPage>('/purchase/orders', {
    query: {
      orderStatus: query.orderStatus,
      pageNo: query.pageNo,
      pageSize: query.pageSize,
    },
  });
}

/**
 * 查询移动端采购订单。
 */
export function getMobilePurchaseOrder(orderNo: string): Promise<MobilePurchaseOrder> {
  return mobileRequest<MobilePurchaseOrder>(`/purchase/orders/${encodeURIComponent(orderNo)}`);
}

/**
 * 移动端确认采购订单。
 */
export function confirmMobilePurchaseOrder(orderNo: string): Promise<MobilePurchaseOrder> {
  return mobileRequest<MobilePurchaseOrder>(`/purchase/orders/${encodeURIComponent(orderNo)}/confirm`, {
    method: 'POST',
  });
}

/**
 * 移动端关闭采购订单。
 */
export function closeMobilePurchaseOrder(orderNo: string): Promise<MobilePurchaseOrder> {
  return mobileRequest<MobilePurchaseOrder>(`/purchase/orders/${encodeURIComponent(orderNo)}/close`, {
    method: 'POST',
  });
}
