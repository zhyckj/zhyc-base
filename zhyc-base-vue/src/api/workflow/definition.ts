/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 工作流流程定义版本。
 */
export interface WorkflowDefinition {
  /** 流程定义主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 流程定义 key。 */
  processKey: string;
  /** 流程定义名称。 */
  processName: string;
  /** 流程定义版本号。 */
  version: number;
  /** Flowable 部署 ID。 */
  deploymentId: string;
  /** 流程定义状态。 */
  status: string;
  /** 备注说明。 */
  remark?: string;
}

/**
 * 工作流流程定义保存参数。
 */
export interface WorkflowDefinitionSavePayload {
  /** 流程定义主键；为空时按租户、流程 key 和版本新增或更新。 */
  id?: number;
  /** 流程定义 key。 */
  processKey: string;
  /** 流程定义名称。 */
  processName: string;
  /** 流程定义版本号。 */
  version: number;
  /** Flowable 部署 ID。 */
  deploymentId: string;
  /** 流程定义状态。 */
  status: string;
  /** 备注说明。 */
  remark?: string;
}

/**
 * 查询租户内工作流流程定义版本。
 *
 * @param tenantId 租户业务编码
 * @returns 流程定义版本列表
 */
export function listWorkflowDefinitions(): Promise<WorkflowDefinition[]> {
  return request<WorkflowDefinition[]>('/workflow/definitions');
}

/**
 * 保存租户内工作流流程定义版本。
 *
 * @param tenantId 租户业务编码
 * @param payload 流程定义保存参数
 * @returns 空响应
 */
export function saveWorkflowDefinition(payload: WorkflowDefinitionSavePayload): Promise<void> {
  return request<void, WorkflowDefinitionSavePayload>('/workflow/definitions', {
    method: 'POST',
    body: payload,
  });
}
