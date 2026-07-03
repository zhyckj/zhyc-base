/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统模块资源。
 */
export interface SystemModuleResource {
  /** 资源类型，例如 menu、permission。 */
  resourceType: string;
  /** 资源编码。 */
  resourceCode: string;
  /** 权限标识。 */
  permission?: string;
  /** 资源名称。 */
  resourceName?: string;
}

/**
 * 系统模块。
 */
export interface SystemModule {
  /** 模块编码。 */
  moduleCode: string;
  /** 模块名称。 */
  moduleName: string;
  /** 模块版本。 */
  version: string;
  /** 模块类型。 */
  moduleType: string;
  /** 是否启用。 */
  enabled: boolean;
  /** 依赖模块编码列表。 */
  dependencies: string[];
  /** 模块资源列表。 */
  resources: SystemModuleResource[];
}

/**
 * 系统模块启停参数。
 */
export interface SystemModuleEnabledPayload {
  /** 是否启用模块。 */
  enabled: boolean;
}

/**
 * 查询系统模块列表。
 *
 * @returns 系统模块列表
 */
export function listSystemModules(): Promise<SystemModule[]> {
  return request<SystemModule[]>('/system/modules');
}

/**
 * 修改系统模块启用状态。
 *
 * @param moduleCode 模块编码
 * @param payload 模块启停参数
 * @returns 空响应
 */
export function changeSystemModuleEnabled(moduleCode: string, payload: SystemModuleEnabledPayload): Promise<void> {
  return request<void, SystemModuleEnabledPayload>(`/system/modules/${moduleCode}/enabled`, {
    method: 'PUT',
    body: payload,
  });
}
