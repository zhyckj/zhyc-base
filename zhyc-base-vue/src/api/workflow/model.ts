/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 工作流流程模型。
 */
export interface WorkflowProcessModel {
  /** 流程模型主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 流程模型编码。 */
  modelCode: string;
  /** 流程模型名称。 */
  modelName: string;
  /** 流程分类 ID。 */
  categoryId?: number;
  /** Flowable 模型 ID。 */
  flowableModelId: string;
  /** BPMN XML 设计稿；由可视化流程编排器保存，发布时可直接复用。 */
  bpmnXml?: string;
  /** 流程模型状态。 */
  status: string;
  /** 创建时间。 */
  createdAt?: string;
  /** 更新时间。 */
  updatedAt?: string;
  /** 备注说明。 */
  remark?: string;
}

/**
 * 工作流流程模型保存参数。
 */
export interface WorkflowProcessModelSavePayload {
  /** 流程模型主键；为空时按租户和流程模型编码新增或更新。 */
  id?: number;
  /** 流程模型编码。 */
  modelCode: string;
  /** 流程模型名称。 */
  modelName: string;
  /** 流程分类 ID。 */
  categoryId?: number;
  /** Flowable 模型 ID。 */
  flowableModelId: string;
  /** BPMN XML 设计稿；为空时仅保存模型基础信息。 */
  bpmnXml?: string;
  /** 流程模型状态。 */
  status: string;
  /** 备注说明。 */
  remark?: string;
}

/**
 * 工作流流程模型发布参数。
 */
export interface WorkflowProcessModelDeployPayload {
  /** BPMN XML 文本；由流程设计器或模型转换器生成。 */
  bpmnXml: string;
  /** 发布备注；写入平台侧流程定义版本。 */
  remark?: string;
}

/**
 * 工作流流程模型发布结果。
 */
export interface WorkflowProcessModelDeployResult {
  /** 租户业务编码。 */
  tenantId: string;
  /** 流程定义 key。 */
  processKey: string;
  /** 流程定义名称。 */
  processName: string;
  /** 平台侧流程定义版本号。 */
  version: number;
  /** Flowable 部署 ID。 */
  deploymentId: string;
}

/**
 * 查询租户内工作流流程模型。
 *
 * @param tenantId 租户业务编码
 * @returns 流程模型列表
 */
export function listWorkflowProcessModels(): Promise<WorkflowProcessModel[]> {
  return request<WorkflowProcessModel[]>('/workflow/models');
}

/**
 * 保存租户内工作流流程模型。
 *
 * @param tenantId 租户业务编码
 * @param payload 流程模型保存参数
 * @returns 空响应
 */
export function saveWorkflowProcessModel(
  payload: WorkflowProcessModelSavePayload,
): Promise<void> {
  return request<void, WorkflowProcessModelSavePayload>('/workflow/models', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 发布租户内工作流流程模型。
 *
 * @param tenantId 租户业务编码
 * @param modelId 流程模型主键
 * @param payload 流程模型发布参数
 * @returns 流程模型发布结果
 */
export function deployWorkflowProcessModel(
  modelId: number,
  payload: WorkflowProcessModelDeployPayload,
): Promise<WorkflowProcessModelDeployResult> {
  return request<WorkflowProcessModelDeployResult, WorkflowProcessModelDeployPayload>(
    `/workflow/models/${modelId}/deploy`,
    {
      method: 'POST',
      body: payload,
    },
  );
}
