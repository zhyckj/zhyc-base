/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 文件存储配置。
 */
export interface FileStorageConfig {
  /** 文件存储配置主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 存储配置编码。 */
  storageCode: string;
  /** 存储配置名称。 */
  storageName: string;
  /** 存储类型。 */
  storageType: string;
  /** 存储端点或本地根路径。 */
  endpoint: string;
  /** 配置状态。 */
  status: string;
  /** 是否默认存储配置。 */
  defaultFlag: boolean;
}

/**
 * 文件存储配置保存参数。
 */
export interface FileStorageConfigSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 存储配置编码。 */
  storageCode: string;
  /** 存储配置名称。 */
  storageName: string;
  /** 存储类型。 */
  storageType: string;
  /** 存储端点或本地根路径。 */
  endpoint: string;
  /** 配置状态。 */
  status?: string;
  /** 是否默认存储配置。 */
  defaultFlag: boolean;
}

/**
 * 查询文件存储配置。
 *
 * @returns 文件存储配置列表
 */
export function listFileStorageConfigs(): Promise<FileStorageConfig[]> {
  return request<FileStorageConfig[]>('/file/storage-configs');
}

/**
 * 保存文件存储配置。
 *
 * @param payload 文件存储配置保存参数
 * @returns 空响应
 */
export function saveFileStorageConfig(payload: FileStorageConfigSavePayload): Promise<void> {
  return request<void, FileStorageConfigSavePayload>('/file/storage-configs', {
    method: 'PUT',
    body: payload,
  });
}
