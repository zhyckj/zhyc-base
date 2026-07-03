/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统组织机构树节点。
 */
export interface SystemOrgTreeNode {
  /** 数据库主键。 */
  id: number;
  /** 父级组织主键。 */
  parentId?: number;
  /** 祖级组织路径。 */
  ancestors?: string;
  /** 组织编码。 */
  orgCode: string;
  /** 组织名称。 */
  orgName: string;
  /** 负责人用户主键。 */
  leaderUserId?: number;
  /** 排序号。 */
  sortOrder?: number;
  /** 组织状态。 */
  status: string;
  /** 子组织节点。 */
  children?: SystemOrgTreeNode[];
}

/**
 * 系统组织机构保存参数。
 */
export interface SystemOrgSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 父级组织主键。 */
  parentId?: number;
  /** 组织编码。 */
  orgCode: string;
  /** 组织名称。 */
  orgName: string;
  /** 负责人用户主键。 */
  leaderUserId?: number;
  /** 排序号。 */
  sortOrder?: number;
  /** 组织状态。 */
  status: string;
}

/**
 * 查询系统组织机构树。
 *
 * @param tenantId 租户业务编码
 * @returns 系统组织机构树
 */
export function listSystemOrgTree(tenantId: string): Promise<SystemOrgTreeNode[]> {
  return request<SystemOrgTreeNode[]>('/system/orgs/tree', {
    query: {
      tenantId,
    },
  });
}

export function createSystemOrg(payload: SystemOrgSavePayload): Promise<void> {
  return request<void, SystemOrgSavePayload>('/system/orgs', {
    method: 'POST',
    body: payload,
  });
}

export function updateSystemOrg(orgId: number, payload: SystemOrgSavePayload): Promise<void> {
  return request<void, SystemOrgSavePayload>(`/system/orgs/${orgId}`, {
    method: 'PUT',
    body: payload,
  });
}

export function updateSystemOrgStatus(orgId: number, tenantId: string, status: string): Promise<void> {
  return request<void, { tenantId: string; status: string }>(`/system/orgs/${orgId}/status`, {
    method: 'PUT',
    body: {
      tenantId,
      status,
    },
  });
}

export function deleteSystemOrg(orgId: number, tenantId: string): Promise<void> {
  return request<void>(`/system/orgs/${orgId}`, {
    method: 'DELETE',
    query: {
      tenantId,
    },
  });
}
