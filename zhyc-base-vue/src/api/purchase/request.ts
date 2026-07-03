/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request, type PageResult } from '@/api/http';

/**
 * 采购申请创建参数。
 */
export interface PurRequestCreateCommand {
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
 * 采购申请提交结果。
 */
export interface PurRequestSubmitResponse {
  /** 采购申请单号。 */
  requestNo: string;
  /** 工作流流程实例 ID。 */
  processInstanceId: string;
  /** 流程状态。 */
  processStatus: string;
}

/**
 * 采购申请状态。
 */
export interface PurRequestStatusResponse {
  /** 采购申请单号。 */
  requestNo: string;
  /** 采购申请标题。 */
  requestTitle: string;
  /** 采购申请总金额。 */
  totalAmount: number;
  /** 流程状态。 */
  processStatus: string;
}

/**
 * 采购申请列表查询参数。
 */
export interface PurRequestListQuery {
  /** 租户编码。 */
  tenantId: string;
  /** 流程状态。 */
  processStatus?: string;
  /** 当前页码。 */
  pageNo: number;
  /** 每页记录数。 */
  pageSize: number;
}

/** 采购申请分页响应。 */
export type PurRequestPageResponse = PageResult<PurRequestStatusResponse>;

/**
 * 分页查询采购申请。
 */
export function listPurchaseRequests(query: PurRequestListQuery): Promise<PurRequestPageResponse> {
  return request<PurRequestPageResponse>('/purchase/requests', {
    query: {
      processStatus: query.processStatus,
      pageNo: query.pageNo,
      pageSize: query.pageSize,
    },
  });
}

/**
 * 创建采购申请。
 */
export function createPurchaseRequest(command: PurRequestCreateCommand): Promise<string> {
  return request<string, PurRequestCreateCommand>('/purchase/requests', {
    method: 'POST',
    body: command,
  });
}

/**
 * 提交采购申请进入审批。
 */
export function submitPurchaseRequest(requestNo: string): Promise<PurRequestSubmitResponse> {
  return request<PurRequestSubmitResponse>(`/purchase/requests/${requestNo}/submit`, {
    method: 'POST',
  });
}

/**
 * 查询采购申请状态。
 */
export function getPurchaseRequestStatus(requestNo: string, tenantId: string): Promise<PurRequestStatusResponse> {
  return request<PurRequestStatusResponse>(`/openapi/v1/purchase/requests/${requestNo}`, {
    query: { tenantId },
  });
}
