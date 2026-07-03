/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统异常日志。
 */
export interface SystemExceptionLog {
  /** 异常日志主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 链路追踪编号。 */
  traceId?: string;
  /** 操作用户主键。 */
  userId?: number;
  /** 操作账号。 */
  username?: string;
  /** 请求地址。 */
  requestUri?: string;
  /** 请求方法。 */
  requestMethod?: string;
  /** 异常类名。 */
  exceptionName?: string;
  /** 异常消息。 */
  message?: string;
  /** 异常堆栈。 */
  stackTrace?: string;
  /** 客户端 IP。 */
  clientIp?: string;
  /** 创建时间。 */
  createdAt: string;
}

/**
 * 查询最近系统异常日志。
 *
 * @param tenantId 租户业务编码
 * @param limit 查询条数上限
 * @returns 系统异常日志列表
 */
export function listSystemExceptionLogs(tenantId: string, limit: number): Promise<SystemExceptionLog[]> {
  return request<SystemExceptionLog[]>('/system/exception-logs/recent', {
    query: {
      tenantId,
      limit,
    },
  });
}
