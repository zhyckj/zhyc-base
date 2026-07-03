/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

export interface AiProviderResponse {
  /** 供应商主键；仅用于模型配置下拉取值，不直接展示为可编辑字段。 */
  id: number;
  /** 供应商编码。 */
  providerCode: string;
  /** 供应商名称。 */
  providerName: string;
  /** 供应商类型。 */
  providerType: string;
  /** 模型服务基础地址。 */
  baseUrl: string;
  /** 密钥中心引用。 */
  secretRef?: string;
  /** 供应商状态。 */
  status: string;
}

export interface AiProviderSaveRequest extends Omit<AiProviderResponse, 'id'> {
  tenantId: string;
  secretRef: string;
}

export interface AiProviderTestResponse {
  /** 供应商编码。 */
  providerCode: string;
  /** 是否可用。 */
  success: boolean;
  /** 测试耗时，单位毫秒。 */
  latencyMs: number;
  /** 测试结果说明。 */
  message: string;
}

export interface AiModelConfigResponse {
  /** 模型配置主键；用于应用接入绑定默认模型。 */
  id: number;
  /** 供应商主键；仅用于下拉回填和提交。 */
  providerId: number;
  /** 模型编码。 */
  modelCode: string;
  /** 模型名称。 */
  modelName: string;
  /** 模型类型。 */
  modelType: string;
  /** 上下文长度。 */
  contextWindow: number;
  /** 是否支持流式输出。 */
  supportStream: boolean;
  /** 是否支持工具调用。 */
  supportTool: boolean;
  /** 模型状态。 */
  status: string;
}

export interface AiModelConfigSaveRequest extends Omit<AiModelConfigResponse, 'id' | 'providerId'> {
  tenantId: string;
  providerId?: number;
}

export interface AiAppResponse {
  appCode: string;
  appName: string;
  defaultModelId: number;
  systemPrompt: string;
  dailyTokenQuota: number;
  status: string;
}

export interface AiAppSaveRequest extends AiAppResponse {
  tenantId: string;
}

export interface AiPromptTemplateResponse {
  promptCode: string;
  promptName: string;
  version: string;
  templateContent: string;
  variables?: string;
  status: string;
}

export interface AiPromptTemplateSaveRequest extends AiPromptTemplateResponse {
  tenantId: string;
}

export interface AiInvocationAuditResponse {
  appCode: string;
  providerId: number;
  modelId: number;
  invocationType: string;
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
  latencyMs: number;
  status: string;
  errorMessage?: string;
  traceId?: string;
}

export interface AiInvocationAuditRecordRequest extends AiInvocationAuditResponse {
  tenantId: string;
}

export interface AiRuntimeChatRequest {
  /** 租户业务编码；用于共享表模式下的数据隔离。 */
  tenantId: string;
  /** AI 应用编码；决定默认模型、系统提示词、配额和审计归属。 */
  appCode: string;
  /** 提示词模板编码；用于渲染业务提示词。 */
  promptCode: string;
  /** 提示词模板版本；为空时后端默认使用 v1。 */
  promptVersion?: string;
  /** 提示词变量值；键名对应模板中的 {{变量名}}。 */
  variables: Record<string, string>;
  /** 是否请求流式输出；当前后台测试调用默认非流式。 */
  stream: boolean;
}

export interface AiRuntimeChatResponse {
  /** AI 应用编码。 */
  appCode: string;
  /** 模型供应商编码。 */
  providerCode: string;
  /** 模型编码。 */
  modelCode: string;
  /** 模型输出内容。 */
  content: string;
  /** 输入令牌数。 */
  promptTokens: number;
  /** 输出令牌数。 */
  completionTokens: number;
  /** 总令牌数。 */
  totalTokens: number;
  /** 调用耗时，单位毫秒。 */
  latencyMs: number;
  /** 调用追踪编号。 */
  traceId?: string;
}

export function listAiProviders(tenantId: string): Promise<AiProviderResponse[]> {
  return request<AiProviderResponse[]>('/ai/providers', { query: { tenantId } });
}

export function saveAiProvider(body: AiProviderSaveRequest): Promise<void> {
  return request<void, AiProviderSaveRequest>('/ai/providers', { method: 'PUT', body });
}

export function testAiProvider(body: AiProviderSaveRequest): Promise<AiProviderTestResponse> {
  return request<AiProviderTestResponse, AiProviderSaveRequest>('/ai/providers/test', { method: 'POST', body });
}

export function listAiModels(tenantId: string): Promise<AiModelConfigResponse[]> {
  return request<AiModelConfigResponse[]>('/ai/models', { query: { tenantId } });
}

export function saveAiModel(body: AiModelConfigSaveRequest): Promise<void> {
  return request<void, AiModelConfigSaveRequest>('/ai/models', { method: 'PUT', body });
}

export function listAiApps(tenantId: string): Promise<AiAppResponse[]> {
  return request<AiAppResponse[]>('/ai/apps', { query: { tenantId } });
}

export function saveAiApp(body: AiAppSaveRequest): Promise<void> {
  return request<void, AiAppSaveRequest>('/ai/apps', { method: 'PUT', body });
}

export function listAiPrompts(tenantId: string): Promise<AiPromptTemplateResponse[]> {
  return request<AiPromptTemplateResponse[]>('/ai/prompts', { query: { tenantId } });
}

export function saveAiPrompt(body: AiPromptTemplateSaveRequest): Promise<void> {
  return request<void, AiPromptTemplateSaveRequest>('/ai/prompts', { method: 'PUT', body });
}

export function listAiInvocationAudits(tenantId: string, appCode: string): Promise<AiInvocationAuditResponse[]> {
  return request<AiInvocationAuditResponse[]>('/ai/invocation-audits', { query: { tenantId, appCode } });
}

export function recordAiInvocationAudit(body: AiInvocationAuditRecordRequest): Promise<void> {
  return request<void, AiInvocationAuditRecordRequest>('/ai/invocation-audits', { method: 'POST', body });
}

export function chatWithAiRuntime(body: AiRuntimeChatRequest): Promise<AiRuntimeChatResponse> {
  return request<AiRuntimeChatResponse, AiRuntimeChatRequest>('/ai/runtime/chat', { method: 'POST', body });
}
