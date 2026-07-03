/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统密钥状态。
 */
export type SystemSecretStatus = 'enabled' | 'disabled';

/**
 * 系统密钥类型。
 */
export type SystemSecretKind = string;

/**
 * 系统密钥响应。
 */
export interface SystemSecretResponse {
  /** 密钥主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 密钥逻辑编码；用于 secret:<secretCode> 引用。 */
  secretCode: string;
  /** 密钥名称。 */
  secretName: string;
  /** 密钥类型。 */
  secretKind: SystemSecretKind;
  /** 密钥状态。 */
  status: SystemSecretStatus;
  /** 到期时间。 */
  expireAt?: string;
  /** 最近轮换时间。 */
  lastRotatedAt?: string;
  /** 创建时间。 */
  createdAt?: string;
  /** 更新时间。 */
  updatedAt?: string;
}

/**
 * 系统密钥选项响应。
 */
export interface SystemSecretOptionResponse {
  /** 密钥逻辑编码。 */
  secretCode: string;
  /** 密钥名称。 */
  secretName: string;
  /** 密钥类型。 */
  secretKind: SystemSecretKind;
  /** 密钥状态。 */
  status: SystemSecretStatus;
  /** 密钥引用。 */
  secretRef: string;
}

/**
 * 系统密钥选择项。
 */
export interface SystemSecretSelectOption {
  /** 下拉展示文案。 */
  label: string;
  /** 下拉取值。 */
  value: string;
  /** 密钥逻辑编码。 */
  secretCode: string;
  /** 密钥名称。 */
  secretName: string;
  /** 密钥类型。 */
  secretKind: SystemSecretKind;
}

/**
 * 系统密钥创建参数。
 */
export interface SystemSecretCreateRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 密钥逻辑编码。 */
  secretCode: string;
  /** 密钥名称。 */
  secretName: string;
  /** 密钥类型。 */
  secretKind: SystemSecretKind;
  /** 密钥明文；仅在提交时传输，不在前端持久化。 */
  secretPlaintext: string;
  /** 密钥状态。 */
  status: SystemSecretStatus;
  /** 到期时间。 */
  expireAt?: string;
}

/**
 * 系统密钥更新参数。
 */
export interface SystemSecretUpdateRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 密钥逻辑编码；编辑时随禁用输入框一并提交，供后端租户内唯一性校验。 */
  secretCode: string;
  /** 密钥名称。 */
  secretName: string;
  /** 密钥类型。 */
  secretKind: SystemSecretKind;
  /** 密钥明文；留空表示仅更新元数据。 */
  secretPlaintext?: string;
  /** 密钥状态。 */
  status: SystemSecretStatus;
  /** 到期时间。 */
  expireAt?: string;
}

/**
 * 系统密钥轮换参数。
 */
export interface SystemSecretRotateRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 新密钥明文。 */
  secretPlaintext: string;
  /** 到期时间。 */
  expireAt?: string;
}

/**
 * 系统密钥状态变更参数。
 */
export interface SystemSecretStatusRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 目标状态。 */
  status: SystemSecretStatus;
}

/**
 * 查询系统密钥列表。
 *
 * @param tenantId 租户业务编码
 * @returns 系统密钥列表
 */
export function listSystemSecrets(tenantId: string): Promise<SystemSecretResponse[]> {
  return request<SystemSecretResponse[]>('/system/secrets', {
    query: {
      tenantId,
    },
  });
}

/**
 * 查询系统密钥下拉选项。
 *
 * @param tenantId 租户业务编码
 * @param secretKind 密钥类型
 * @param status 密钥状态
 * @returns 系统密钥选项列表
 */
export function listSystemSecretOptions(
  tenantId: string,
  secretKind?: SystemSecretKind,
  status?: SystemSecretStatus,
): Promise<SystemSecretOptionResponse[]> {
  return request<SystemSecretOptionResponse[]>('/system/secrets/options', {
    query: {
      tenantId,
      secretKind,
      status,
    },
  });
}

/**
 * 将系统密钥选项转换为前端下拉项。
 *
 * @param options 系统密钥选项列表
 * @returns 前端下拉项
 */
export function buildSystemSecretSelectOptions(options: SystemSecretOptionResponse[]): SystemSecretSelectOption[] {
  return options.map((option) => {
    const secretRef = buildSystemSecretRef(option.secretCode);
    return {
      label: `${option.secretName}（${secretRef}）`,
      value: secretRef,
      secretCode: option.secretCode,
      secretName: option.secretName,
      secretKind: option.secretKind,
    };
  });
}

/**
 * 创建系统密钥。
 *
 * @param payload 系统密钥创建参数
 * @returns 空响应
 */
export function createSystemSecret(payload: SystemSecretCreateRequest): Promise<void> {
  return request<void, SystemSecretCreateRequest>('/system/secrets', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 修改系统密钥。
 *
 * @param secretId 密钥主键
 * @param payload 系统密钥更新参数
 * @returns 空响应
 */
export function updateSystemSecret(secretId: number, payload: SystemSecretUpdateRequest): Promise<void> {
  return request<void, SystemSecretUpdateRequest>(`/system/secrets/${secretId}`, {
    method: 'PUT',
    body: payload,
  });
}

/**
 * 修改系统密钥状态。
 *
 * @param secretId 密钥主键
 * @param payload 系统密钥状态变更参数
 * @returns 空响应
 */
export function changeSystemSecretStatus(secretId: number, payload: SystemSecretStatusRequest): Promise<void> {
  return request<void, SystemSecretStatusRequest>(`/system/secrets/${secretId}/status`, {
    method: 'PUT',
    body: payload,
  });
}

/**
 * 轮换系统密钥。
 *
 * @param secretId 密钥主键
 * @param payload 系统密钥轮换参数
 * @returns 空响应
 */
export function rotateSystemSecret(secretId: number, payload: SystemSecretRotateRequest): Promise<void> {
  return request<void, SystemSecretRotateRequest>(`/system/secrets/${secretId}/rotate`, {
    method: 'POST',
    body: payload,
  });
}

/**
 * 删除系统密钥。
 *
 * @param secretId 密钥主键
 * @param tenantId 租户业务编码
 * @returns 空响应
 */
export function deleteSystemSecret(secretId: number, tenantId: string): Promise<void> {
  return request<void>(`/system/secrets/${secretId}`, {
    method: 'DELETE',
    query: {
      tenantId,
    },
  });
}

/**
 * 构建系统密钥引用值。
 *
 * @param secretCode 密钥逻辑编码
 * @returns secret:<secretCode> 引用值
 */
export function buildSystemSecretRef(secretCode: string): string {
  const normalizedSecretCode = secretCode.trim();
  return `secret:${normalizedSecretCode}`;
}
