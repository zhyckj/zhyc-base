/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 可视化数据集。
 */
export interface VisualDataset {
  /** 数据集主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 数据集编码。 */
  datasetCode: string;
  /** 数据集名称。 */
  datasetName: string;
  /** 数据源编码。 */
  datasourceCode: string;
  /** 查询 SQL。 */
  sqlText: string;
  /** 数据集状态。 */
  status: string;
}

/**
 * 可视化数据集预览结果。
 */
export interface VisualDatasetPreview {
  /** 数据集编码。 */
  datasetCode: string;
  /** 可绑定字段列表。 */
  columns: string[];
  /** 预览数据行。 */
  rows: Record<string, unknown>[];
  /** 是否已执行真实数据源查询。 */
  executable: boolean;
  /** 预览说明。 */
  message: string;
}

/**
 * 可视化报表。
 */
export interface VisualReport {
  /** 报表主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 报表编码。 */
  reportCode: string;
  /** 报表名称。 */
  reportName: string;
  /** 数据集编码。 */
  datasetCode: string;
  /** 图表类型。 */
  chartType: string;
  /** 图表配置 JSON。 */
  configJson: string;
  /** 报表状态。 */
  status: string;
}

/**
 * 可视化大屏。
 */
export interface VisualScreen {
  /** 大屏主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 大屏编码。 */
  screenCode: string;
  /** 大屏名称。 */
  screenName: string;
  /** 布局 JSON。 */
  layoutJson: string;
  /** 大屏状态。 */
  status: string;
}

/**
 * 数据集保存参数。
 */
export interface VisualDatasetSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 数据集编码。 */
  datasetCode: string;
  /** 数据集名称。 */
  datasetName: string;
  /** 数据源编码。 */
  datasourceCode: string;
  /** 查询 SQL。 */
  sqlText: string;
  /** 数据集状态。 */
  status?: string;
}

/**
 * 报表保存参数。
 */
export interface VisualReportSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 报表编码。 */
  reportCode: string;
  /** 报表名称。 */
  reportName: string;
  /** 数据集编码。 */
  datasetCode: string;
  /** 图表类型。 */
  chartType?: string;
  /** 图表配置 JSON。 */
  configJson?: string;
  /** 报表状态。 */
  status?: string;
}

/**
 * 大屏保存参数。
 */
export interface VisualScreenSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 大屏编码。 */
  screenCode: string;
  /** 大屏名称。 */
  screenName: string;
  /** 布局 JSON。 */
  layoutJson?: string;
  /** 大屏状态。 */
  status?: string;
}

/**
 * 查询可视化数据集列表。
 *
 * @param status 数据集状态
 * @returns 数据集列表
 */
export function listVisualDatasets(status?: string): Promise<VisualDataset[]> {
  return request<VisualDataset[]>('/visual/datasets', {
    query: { status },
  });
}

/**
 * 预览可视化数据集字段和样例数据。
 *
 * @param datasetCode 数据集编码
 * @param limit 预览行数上限
 * @returns 数据集预览结果
 */
export function previewVisualDataset(datasetCode: string, limit = 20): Promise<VisualDatasetPreview> {
  return request<VisualDatasetPreview>(`/visual/datasets/${encodeURIComponent(datasetCode)}/preview`, {
    query: { limit },
  });
}

/**
 * 保存可视化数据集。
 *
 * @param payload 数据集保存参数
 */
export function saveVisualDataset(payload: VisualDatasetSavePayload): Promise<void> {
  return request<void, VisualDatasetSavePayload>('/visual/datasets', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 查询可视化报表列表。
 *
 * @param status 报表状态
 * @returns 报表列表
 */
export function listVisualReports(status?: string): Promise<VisualReport[]> {
  return request<VisualReport[]>('/visual/reports', {
    query: { status },
  });
}

/**
 * 保存可视化报表。
 *
 * @param payload 报表保存参数
 */
export function saveVisualReport(payload: VisualReportSavePayload): Promise<void> {
  return request<void, VisualReportSavePayload>('/visual/reports', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 查询公开访问的已发布报表。
 *
 * @param tenantId 租户业务编码
 * @param reportCode 报表编码
 * @returns 已发布报表
 */
export function getPublishedVisualReport(tenantId: string, reportCode: string): Promise<VisualReport> {
  return request<VisualReport>(
    `/visual/public/reports/${encodeURIComponent(tenantId)}/${encodeURIComponent(reportCode)}`,
    { anonymous: true },
  );
}

/**
 * 预览公开报表引用的数据集。
 *
 * @param tenantId 租户业务编码
 * @param reportCode 报表编码
 * @param datasetCode 数据集编码
 * @param limit 预览行数上限
 * @returns 数据集预览结果
 */
export function previewPublishedReportDataset(
  tenantId: string,
  reportCode: string,
  datasetCode: string,
  limit = 20,
): Promise<VisualDatasetPreview> {
  return request<VisualDatasetPreview>(
    `/visual/public/reports/${encodeURIComponent(tenantId)}/${encodeURIComponent(reportCode)}/datasets/${encodeURIComponent(datasetCode)}/preview`,
    { anonymous: true, query: { limit } },
  );
}

/**
 * 变更可视化报表状态。
 *
 * @param id 报表主键
 * @param status 目标状态
 */
export function changeVisualReportStatus(id: number, status: string): Promise<void> {
  return request<void, { status: string }>(`/visual/reports/${id}/status`, {
    method: 'POST',
    body: { status },
  });
}

/**
 * 查询可视化大屏列表。
 *
 * @param status 大屏状态
 * @returns 大屏列表
 */
export function listVisualScreens(status?: string): Promise<VisualScreen[]> {
  return request<VisualScreen[]>('/visual/screens', {
    query: { status },
  });
}

/**
 * 查询公开访问的已发布大屏。
 *
 * @param tenantId 租户业务编码
 * @param screenCode 大屏编码
 * @returns 已发布大屏
 */
export function getPublishedVisualScreen(tenantId: string, screenCode: string): Promise<VisualScreen> {
  return request<VisualScreen>(
    `/visual/public/screens/${encodeURIComponent(tenantId)}/${encodeURIComponent(screenCode)}`,
    { anonymous: true },
  );
}

/**
 * 预览公开大屏引用的数据集。
 *
 * @param tenantId 租户业务编码
 * @param screenCode 大屏编码
 * @param datasetCode 数据集编码
 * @param limit 预览行数上限
 * @returns 数据集预览结果
 */
export function previewPublishedScreenDataset(
  tenantId: string,
  screenCode: string,
  datasetCode: string,
  limit = 20,
): Promise<VisualDatasetPreview> {
  return request<VisualDatasetPreview>(
    `/visual/public/screens/${encodeURIComponent(tenantId)}/${encodeURIComponent(screenCode)}/datasets/${encodeURIComponent(datasetCode)}/preview`,
    { anonymous: true, query: { limit } },
  );
}

/**
 * 保存可视化大屏。
 *
 * @param payload 大屏保存参数
 */
export function saveVisualScreen(payload: VisualScreenSavePayload): Promise<void> {
  return request<void, VisualScreenSavePayload>('/visual/screens', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 变更可视化大屏状态。
 *
 * @param id 大屏主键
 * @param status 目标状态
 */
export function changeVisualScreenStatus(id: number, status: string): Promise<void> {
  return request<void, { status: string }>(`/visual/screens/${id}/status`, {
    method: 'POST',
    body: { status },
  });
}
