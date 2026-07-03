/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 代码生成目标端。
 */
export type LowcodeGenerationTarget =
  | 'ADMIN_BACKEND'
  | 'ADMIN_FRONTEND'
  | 'UNIAPP'
  | 'OPEN_API_PORTAL'
  | 'MICROSERVICE_MODULE';

/**
 * 生成文件覆盖策略。
 */
export type LowcodeOverwriteStrategy = 'FAIL_IF_EXISTS' | 'OVERWRITE';

/**
 * 低代码模板响应。
 */
export interface LowcodeTemplateResponse {
  /** 模板唯一编码。 */
  code: string;
  /** 生成目标端编码。 */
  target: LowcodeGenerationTarget;
  /** 模板名称。 */
  name: string;
  /** 输出路径模式。 */
  outputPathPattern: string;
}

/**
 * 低代码生成请求基础参数。
 */
export interface LowcodeGenerationPreviewRequest {
  /** 租户编码。 */
  tenantId: string;
  /** 表模型编码。 */
  tableModelCode: string;
  /** 生成目标端编码。 */
  target: LowcodeGenerationTarget;
  /** 业务模块名称。 */
  moduleName: string;
  /** 业务实体名称。 */
  entityName: string;
}

/**
 * 低代码生成执行请求。
 */
export interface LowcodeGenerationExecuteRequest extends LowcodeGenerationPreviewRequest {
  /** 生成文件覆盖策略。 */
  overwriteStrategy: LowcodeOverwriteStrategy;
}

/**
 * 生成文件预览响应。
 */
export interface LowcodeGeneratedFileResponse {
  /** 生成目标端编码。 */
  target: LowcodeGenerationTarget;
  /** 使用的模板编码。 */
  templateCode: string;
  /** 相对输出路径。 */
  path: string;
  /** 文件内容哈希。 */
  contentHash: string;
  /** 文件内容。 */
  content: string;
}

/**
 * 低代码生成前校验项。
 */
export interface LowcodeGenerationValidationItem {
  /** 校验项编码；用于前端分组展示和后续国际化映射。 */
  code: string;
  /** 校验项消息；说明阻断原因或整改建议。 */
  message: string;
}

/**
 * 低代码生成前校验结果。
 */
export interface LowcodeGenerationValidationResult {
  /** 是否允许继续预览或执行生成。 */
  passed: boolean;
  /** 阻断生成的错误项列表。 */
  errors: LowcodeGenerationValidationItem[];
  /** 不阻断生成的警告项列表。 */
  warnings: LowcodeGenerationValidationItem[];
}

/**
 * 低代码生成记录响应。
 */
export interface LowcodeGenerationRecordResponse {
  /** 生成记录主键。 */
  id: number;
  /** 租户编码。 */
  tenantId: string;
  /** 表模型编码。 */
  tableModelCode: string;
  /** 生成目标端编码。 */
  target: LowcodeGenerationTarget;
  /** 业务模块名称。 */
  moduleName: string;
  /** 业务实体名称。 */
  entityName: string;
  /** 覆盖策略编码。 */
  overwriteStrategy: LowcodeOverwriteStrategy;
  /** 生成文件数量。 */
  fileCount: number;
  /** 生成文件清单 JSON。 */
  fileManifestJson: string;
  /** 生成记录状态。 */
  status: string;
  /** 失败原因。 */
  errorMessage?: string;
}

/**
 * 低代码生成文件明细响应。
 */
export interface LowcodeGenerationFileResponse {
  /** 生成文件明细主键。 */
  id: number;
  /** 生成记录主键。 */
  recordId: number;
  /** 模板编码。 */
  templateCode: string;
  /** 生成文件路径。 */
  filePath: string;
  /** 生成文件类型。 */
  fileType: string;
  /** 覆盖模式。 */
  overwriteMode: LowcodeOverwriteStrategy;
  /** 文件内容哈希。 */
  contentHash: string;
}

/**
 * 查询目标端模板清单。
 */
export function listGeneratorTemplates(target: LowcodeGenerationTarget): Promise<LowcodeTemplateResponse[]> {
  return request<LowcodeTemplateResponse[]>('/lowcode/generator/templates', {
    query: { target },
  });
}

/**
 * 预览生成文件。
 */
export function previewGeneratedFiles(
  command: LowcodeGenerationPreviewRequest,
): Promise<LowcodeGeneratedFileResponse[]> {
  return request<LowcodeGeneratedFileResponse[], LowcodeGenerationPreviewRequest>('/lowcode/generator/preview', {
    method: 'POST',
    body: command,
  });
}

/**
 * 执行生成前校验。
 */
export function validateGeneratedFiles(
  command: LowcodeGenerationPreviewRequest,
): Promise<LowcodeGenerationValidationResult> {
  return request<LowcodeGenerationValidationResult, LowcodeGenerationPreviewRequest>('/lowcode/generator/validate', {
    method: 'POST',
    body: command,
  });
}

/**
 * 执行代码生成。
 */
export function executeGeneration(
  command: LowcodeGenerationExecuteRequest,
): Promise<LowcodeGenerationRecordResponse> {
  return request<LowcodeGenerationRecordResponse, LowcodeGenerationExecuteRequest>('/lowcode/generator/execute', {
    method: 'POST',
    body: command,
  });
}

/**
 * 查询租户内生成记录。
 */
export function listGenerationRecords(tenantId: string): Promise<LowcodeGenerationRecordResponse[]> {
  return request<LowcodeGenerationRecordResponse[]>('/lowcode/generator/records', {
    query: { tenantId },
  });
}

/**
 * 查询生成记录对应的文件明细。
 */
export function listGenerationFiles(tenantId: string, recordId: number): Promise<LowcodeGenerationFileResponse[]> {
  return request<LowcodeGenerationFileResponse[]>('/lowcode/generator/files', {
    query: { tenantId, recordId },
  });
}
