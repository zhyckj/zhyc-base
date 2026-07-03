/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统岗位。
 */
export interface SystemPost {
  /** 岗位主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 所属组织主键。 */
  orgId?: number;
  /** 岗位编码。 */
  postCode: string;
  /** 岗位名称。 */
  postName: string;
  /** 排序号。 */
  sortOrder?: number;
  /** 岗位状态。 */
  status: string;
}

/**
 * 系统岗位保存参数。
 */
export interface SystemPostSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 所属组织主键。 */
  orgId?: number;
  /** 岗位编码。 */
  postCode: string;
  /** 岗位名称。 */
  postName: string;
  /** 排序号。 */
  sortOrder?: number;
  /** 岗位状态。 */
  status: string;
}

/**
 * 查询系统岗位列表。
 *
 * @param tenantId 租户业务编码
 * @param orgId 所属组织主键
 * @returns 系统岗位列表
 */
export function listSystemPosts(tenantId: string, orgId?: number): Promise<SystemPost[]> {
  return request<SystemPost[]>('/system/posts', {
    query: {
      tenantId,
      orgId,
    },
  });
}

export function createSystemPost(payload: SystemPostSavePayload): Promise<void> {
  return request<void, SystemPostSavePayload>('/system/posts', {
    method: 'POST',
    body: payload,
  });
}

export function updateSystemPost(postId: number, payload: SystemPostSavePayload): Promise<void> {
  return request<void, SystemPostSavePayload>(`/system/posts/${postId}`, {
    method: 'PUT',
    body: payload,
  });
}

export function updateSystemPostStatus(postId: number, tenantId: string, status: string): Promise<void> {
  return request<void, { tenantId: string; status: string }>(`/system/posts/${postId}/status`, {
    method: 'PUT',
    body: {
      tenantId,
      status,
    },
  });
}

export function deleteSystemPost(postId: number, tenantId: string): Promise<void> {
  return request<void>(`/system/posts/${postId}`, {
    method: 'DELETE',
    query: {
      tenantId,
    },
  });
}
