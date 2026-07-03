/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 工作流表单绑定。
 */
export interface WorkflowFormBinding {
  /** 绑定主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 流程定义 key。 */
  processKey: string;
  /** 业务模块编码。 */
  businessModule: string;
  /** 业务表名。 */
  businessTable: string;
  /** 后台表单路由。 */
  formRoute: string;
  /** 移动端表单路由。 */
  mobileRoute?: string;
  /** 绑定状态。 */
  status: string;
  /** 备注说明。 */
  remark?: string;
}

/**
 * 工作流表单绑定保存参数。
 */
export interface WorkflowFormBindingSavePayload {
  /** 绑定主键；为空时按租户和流程 key 新增或更新。 */
  id?: number;
  /** 流程定义 key。 */
  processKey: string;
  /** 业务模块编码。 */
  businessModule: string;
  /** 业务表名。 */
  businessTable: string;
  /** 后台表单路由。 */
  formRoute: string;
  /** 移动端表单路由。 */
  mobileRoute?: string;
  /** 绑定状态。 */
  status: string;
  /** 备注说明。 */
  remark?: string;
}

/**
 * 查询租户内工作流表单绑定。
 *
 * @param tenantId 租户业务编码
 * @returns 工作流表单绑定列表
 */
export function listWorkflowFormBindings(): Promise<WorkflowFormBinding[]> {
  return request<WorkflowFormBinding[]>('/workflow/form-bindings');
}

/**
 * 保存租户内工作流表单绑定。
 *
 * @param tenantId 租户业务编码
 * @param payload 工作流表单绑定保存参数
 * @returns 空响应
 */
export function saveWorkflowFormBinding(payload: WorkflowFormBindingSavePayload): Promise<void> {
  return request<void, WorkflowFormBindingSavePayload>('/workflow/form-bindings', {
    method: 'POST',
    body: payload,
  });
}
