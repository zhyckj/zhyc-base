/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 全文检索索引配置。
 */
export interface SearchIndexConfig {
  /** 索引配置主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 索引编码。 */
  indexCode: string;
  /** 索引名称。 */
  indexName: string;
  /** 数据来源表名。 */
  sourceTable: string;
  /** 可检索字段列表。 */
  searchFields: string;
  /** 可过滤字段列表。 */
  filterFields?: string;
  /** 索引状态。 */
  status: string;
  /** 配置备注。 */
  remark?: string;
}

/**
 * 全文检索索引配置保存参数。
 */
export interface SearchIndexConfigSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 索引编码。 */
  indexCode: string;
  /** 索引名称。 */
  indexName: string;
  /** 数据来源表名。 */
  sourceTable: string;
  /** 可检索字段列表。 */
  searchFields: string;
  /** 可过滤字段列表。 */
  filterFields?: string;
  /** 索引状态。 */
  status?: string;
  /** 配置备注。 */
  remark?: string;
}

/**
 * 全文检索索引重建任务。
 */
export interface SearchRebuildTask {
  /** 重建任务主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 索引编码。 */
  indexCode: string;
  /** 任务状态。 */
  taskStatus: string;
  /** 触发类型。 */
  triggerType: string;
  /** 创建时间。 */
  createdAt?: string;
}

/**
 * 全文检索查询日志。
 */
export interface SearchQueryLog {
  /** 查询日志主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 索引编码。 */
  indexCode: string;
  /** 查询关键词。 */
  keyword: string;
  /** 返回结果数量。 */
  resultCount: number;
  /** 查询耗时毫秒。 */
  costMs: number;
  /** 查询状态。 */
  queryStatus: string;
  /** 创建时间。 */
  createdAt?: string;
}

/**
 * 全文检索查询响应。
 */
export interface SearchQueryResult {
  /** 索引编码。 */
  indexCode: string;
  /** 查询关键词。 */
  keyword: string;
  /** 命中数量。 */
  total: number;
  /** 命中记录。 */
  items: string[];
}

/**
 * 查询全文检索索引配置列表。
 *
 * @param status 索引状态
 * @returns 索引配置列表
 */
export function listSearchIndexConfigs(status?: string): Promise<SearchIndexConfig[]> {
  return request<SearchIndexConfig[]>('/search/index-configs', {
    query: {
      status,
    },
  });
}

/**
 * 保存全文检索索引配置。
 *
 * @param payload 索引配置保存参数
 */
export function saveSearchIndexConfig(payload: SearchIndexConfigSavePayload): Promise<void> {
  return request<void, SearchIndexConfigSavePayload>('/search/index-configs', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 创建索引重建任务。
 *
 * @param indexCode 索引编码
 * @param tenantId 当前租户业务编码
 */
export function createSearchRebuildTask(indexCode: string, tenantId: string): Promise<void> {
  return request<void, { tenantId: string; indexCode: string; triggerType: string }>('/search/rebuild-tasks', {
    method: 'POST',
    body: {
      tenantId,
      indexCode,
      triggerType: 'manual',
    },
  });
}

/**
 * 查询索引重建任务列表。
 *
 * @param indexCode 索引编码
 * @returns 重建任务列表
 */
export function listSearchRebuildTasks(indexCode?: string): Promise<SearchRebuildTask[]> {
  return request<SearchRebuildTask[]>('/search/rebuild-tasks', {
    query: {
      indexCode,
    },
  });
}

/**
 * 执行全文检索查询。
 *
 * @param indexCode 索引编码
 * @param keyword 查询关键词
 * @param tenantId 当前租户业务编码
 * @returns 查询结果
 */
export function executeSearchQuery(indexCode: string, keyword: string, tenantId: string): Promise<SearchQueryResult> {
  return request<SearchQueryResult, { tenantId: string; indexCode: string; keyword: string }>('/search/query', {
    method: 'POST',
    body: {
      tenantId,
      indexCode,
      keyword,
    },
  });
}

/**
 * 查询全文检索日志列表。
 *
 * @param indexCode 索引编码
 * @returns 查询日志列表
 */
export function listSearchQueryLogs(indexCode?: string): Promise<SearchQueryLog[]> {
  return request<SearchQueryLog[]>('/search/query-logs', {
    query: {
      indexCode,
    },
  });
}
