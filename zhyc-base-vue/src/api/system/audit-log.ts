/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统审计日志。
 */
export interface SystemAuditLog {
  /** 审计日志主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 操作用户主键。 */
  userId?: number;
  /** 操作账号。 */
  username?: string;
  /** 操作动作编码。 */
  action: string;
  /** 被操作目标类型。 */
  targetType?: string;
  /** 被操作目标标识。 */
  targetId?: string;
  /** 操作结果。 */
  result: string;
  /** 客户端 IP。 */
  clientIp?: string;
  /** 操作详情或失败原因。 */
  detail?: string;
  /** 审计日志创建时间。 */
  createdAt: string;
}

/**
 * 查询最近系统审计日志。
 *
 * @param tenantId 租户业务编码
 * @param limit 查询条数上限
 * @returns 系统审计日志列表
 */
export function listSystemAuditLogs(tenantId: string, limit: number): Promise<SystemAuditLog[]> {
  return request<SystemAuditLog[]>('/system/audit-logs/recent', {
    query: {
      tenantId,
      limit,
    },
  });
}
