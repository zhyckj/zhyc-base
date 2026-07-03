/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { listGenerationRecords } from '@/api/lowcode/generator';
import { listOpenApiApps } from '@/api/openapi/app';
import { listOpenApiCallAudits } from '@/api/openapi/call-audit';
import { listPurchaseRequests } from '@/api/purchase/request';
import { listTodoTasks } from '@/api/workflow/task';

/**
 * 工作台概览查询参数。
 */
export interface DashboardOverviewQuery {
  /** 租户编码。 */
  tenantId: string;
  /** 当前登录用户 ID。 */
  userId: number;
}

/**
 * 工作台指标项。
 */
export interface DashboardMetricItem {
  /** 指标标题。 */
  title: string;
  /** 指标数值。 */
  value: number;
  /** 指标单位。 */
  suffix: string;
}

/**
 * 工作台概览响应。
 */
export interface DashboardOverview {
  /** 指标列表。 */
  metrics: DashboardMetricItem[];
  /** 局部加载失败的提示信息。 */
  warnings: string[];
}

/**
 * 查询工作台概览指标。
 *
 * <p>首期复用已有模块 API 聚合指标，避免新增后端接口；单个模块失败时返回局部告警，保证工作台可用。</p>
 *
 * @param query 工作台概览查询参数
 * @returns 工作台概览指标和局部告警
 */
export async function getDashboardOverview(query: DashboardOverviewQuery): Promise<DashboardOverview> {
  const [todoResult, requestResult, openApiResult, generationResult] = await Promise.allSettled([
    listTodoTasks(),
    listPurchaseRequests({ tenantId: query.tenantId, pageNo: 1, pageSize: 1 }),
    countOpenApiCalls(query.tenantId),
    listGenerationRecords(query.tenantId),
  ]);

  const warnings: string[] = [];

  const todoCount = readSettledValue(todoResult, '待办任务加载失败', warnings)?.length ?? 0;
  const requestCount = readSettledValue(requestResult, '我的申请加载失败', warnings)?.total ?? 0;
  const openApiCallCount = readSettledValue(openApiResult, '开放 API 调用加载失败', warnings) ?? 0;
  const generationCount = readSettledValue(generationResult, '生成记录加载失败', warnings)?.length ?? 0;

  return {
    metrics: [
      { title: '待办任务', value: todoCount, suffix: '条' },
      { title: '我的申请', value: requestCount, suffix: '条' },
      { title: '开放 API 调用', value: openApiCallCount, suffix: '次' },
      { title: '生成记录', value: generationCount, suffix: '次' },
    ],
    warnings,
  };
}

/**
 * 统计租户下开放 API 调用记录数。
 *
 * @param tenantId 租户编码
 * @returns 开放 API 调用记录数
 */
async function countOpenApiCalls(tenantId: string): Promise<number> {
  const apps = await listOpenApiApps(tenantId);
  const auditResults = await Promise.all(
    apps.map((app) => listOpenApiCallAudits(tenantId, app.appCode)),
  );
  return auditResults.reduce((total, audits) => total + audits.length, 0);
}

/**
 * 读取 Promise.allSettled 的成功值，并收集失败提示。
 *
 * @param result 异步调用结果
 * @param warningMessage 失败提示
 * @param warnings 失败提示集合
 * @returns 成功值；失败时返回空
 */
function readSettledValue<T>(
  result: PromiseSettledResult<T>,
  warningMessage: string,
  warnings: string[],
): T | undefined {
  if (result.status === 'fulfilled') {
    return result.value;
  }
  warnings.push(warningMessage);
  return undefined;
}
