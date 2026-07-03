/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 工作流分类。
 */
export interface WorkflowCategory {
  /** 分类主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 流程分类编码。 */
  categoryCode: string;
  /** 流程分类名称。 */
  categoryName: string;
  /** 排序号。 */
  sortOrder: number;
  /** 分类状态。 */
  status: string;
  /** 创建时间。 */
  createdAt?: string;
  /** 更新时间。 */
  updatedAt?: string;
  /** 备注说明。 */
  remark?: string;
}

/**
 * 工作流分类保存参数。
 */
export interface WorkflowCategorySavePayload {
  /** 分类主键；为空时按租户和分类编码新增或更新。 */
  id?: number;
  /** 流程分类编码。 */
  categoryCode: string;
  /** 流程分类名称。 */
  categoryName: string;
  /** 排序号。 */
  sortOrder: number;
  /** 分类状态。 */
  status: string;
  /** 备注说明。 */
  remark?: string;
}

/**
 * 查询租户内工作流分类。
 *
 * @param tenantId 租户业务编码
 * @returns 工作流分类列表
 */
export function listWorkflowCategories(): Promise<WorkflowCategory[]> {
  return request<WorkflowCategory[]>('/workflow/categories');
}

/**
 * 保存租户内工作流分类。
 *
 * @param tenantId 租户业务编码
 * @param payload 工作流分类保存参数
 * @returns 空响应
 */
export function saveWorkflowCategory(payload: WorkflowCategorySavePayload): Promise<void> {
  return request<void, WorkflowCategorySavePayload>('/workflow/categories', {
    method: 'POST',
    body: payload,
  });
}
