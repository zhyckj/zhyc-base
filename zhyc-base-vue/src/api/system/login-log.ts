/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统登录日志。
 */
export interface SystemLoginLog {
  /** 登录日志主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 登录用户主键。 */
  userId?: number;
  /** 登录账号。 */
  username?: string;
  /** 登录方式。 */
  loginType?: string;
  /** 登录结果。 */
  result: string;
  /** 客户端 IP。 */
  clientIp?: string;
  /** 浏览器或客户端 User-Agent。 */
  userAgent?: string;
  /** 创建时间。 */
  createdAt: string;
}

/**
 * 查询最近系统登录日志。
 *
 * @param tenantId 租户业务编码
 * @param limit 查询条数上限
 * @returns 系统登录日志列表
 */
export function listSystemLoginLogs(tenantId: string, limit: number): Promise<SystemLoginLog[]> {
  return request<SystemLoginLog[]>('/system/login-logs/recent', {
    query: {
      tenantId,
      limit,
    },
  });
}
