/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统权限变更审计。
 */
export interface SystemPermissionAudit {
  /** 权限变更审计主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 操作者用户主键。 */
  operatorId?: number;
  /** 目标类型。 */
  targetType: string;
  /** 目标业务标识。 */
  targetId: string;
  /** 变更前内容。 */
  beforeValue?: string;
  /** 变更后内容。 */
  afterValue?: string;
  /** 变更类型。 */
  changeType: string;
  /** 创建时间。 */
  createdAt: string;
}

/**
 * 查询最近系统权限变更审计。
 *
 * @param tenantId 租户业务编码
 * @param limit 查询条数上限
 * @returns 系统权限变更审计列表
 */
export function listSystemPermissionAudits(tenantId: string, limit: number): Promise<SystemPermissionAudit[]> {
  return request<SystemPermissionAudit[]>('/system/permission-audits/recent', {
    query: {
      tenantId,
      limit,
    },
  });
}
