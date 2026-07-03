/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 服务运行状态。
 */
export interface RuntimeServiceStatus {
  /** 服务名称。 */
  serviceName: string;
  /** 运行状态。 */
  status: string;
  /** 服务版本。 */
  version: string;
  /** 服务健康检测响应耗时，单位毫秒。 */
  responseTimeMs: number;
  /** 最近心跳时间。 */
  heartbeatAt: string;
}

/**
 * 数据源运行状态。
 */
export interface RuntimeDataSourceStatus {
  /** 数据源编码。 */
  sourceCode: string;
  /** 数据源名称。 */
  sourceName: string;
  /** 连接状态。 */
  status: string;
  /** 最近检测耗时，单位毫秒。 */
  costMs: number;
  /** 最近检测时间。 */
  checkedAt: string;
}

/**
 * SQL 执行效率监控记录。
 */
export interface RuntimeSqlMonitorRecord {
  /** 数据源编码。 */
  sourceCode: string;
  /** 归一化 SQL 摘要，不包含明文参数。 */
  sqlDigest: string;
  /** 执行次数。 */
  executeCount: number;
  /** 平均执行耗时，单位毫秒。 */
  avgCostMs: number;
  /** 最大执行耗时，单位毫秒。 */
  maxCostMs: number;
  /** 扫描行数。 */
  rowsExamined: number;
  /** 返回行数。 */
  rowsSent: number;
  /** 慢 SQL 等级。 */
  severity: 'CRITICAL' | 'SLOW' | 'NORMAL' | 'UNAVAILABLE';
  /** 优化建议。 */
  suggestion: string;
  /** 最近一次采集时间。 */
  lastSeen: string;
}

/**
 * 查询服务运行状态。
 *
 * @returns 服务运行状态列表
 */
export function listRuntimeServiceStatus(): Promise<RuntimeServiceStatus[]> {
  return request<RuntimeServiceStatus[]>('/monitor/runtime/services');
}

/**
 * 查询数据源运行状态。
 *
 * @returns 数据源运行状态列表
 */
export function listRuntimeDataSourceStatus(): Promise<RuntimeDataSourceStatus[]> {
  return request<RuntimeDataSourceStatus[]>('/monitor/runtime/data-sources');
}

/**
 * 查询 SQL 执行效率监控记录。
 *
 * @param thresholdMs 慢 SQL 平均耗时阈值，单位毫秒
 * @param limit 最大返回记录数
 * @returns SQL 执行效率监控记录列表
 */
export function listRuntimeSqlMonitorRecords(thresholdMs = 1, limit = 20): Promise<RuntimeSqlMonitorRecord[]> {
  return request<RuntimeSqlMonitorRecord[]>('/monitor/runtime/sql', {
    query: {
      thresholdMs,
      limit,
    },
  });
}
