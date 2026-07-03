/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统字典类型。
 */
export interface SystemDictType {
  /** 字典类型主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 字典编码。 */
  dictCode: string;
  /** 字典名称。 */
  dictName: string;
  /** 是否系统内置字典。 */
  systemFlag: boolean;
  /** 字典状态。 */
  status: string;
}

/**
 * 系统字典项。
 */
export interface SystemDictItem {
  /** 字典项主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 字典编码。 */
  dictCode: string;
  /** 字典项显示标签。 */
  itemLabel: string;
  /** 字典项实际值。 */
  itemValue: string;
  /** 字典项前端展示颜色。 */
  itemColor?: string;
  /** 字典项排序号。 */
  sortOrder?: number;
  /** 字典项状态。 */
  status: string;
}

export interface SystemDictTypeSavePayload {
  tenantId: string;
  dictCode: string;
  dictName: string;
  systemFlag: boolean;
  status: string;
}

export interface SystemDictItemSavePayload {
  tenantId: string;
  dictCode: string;
  itemLabel: string;
  itemValue: string;
  itemColor?: string;
  sortOrder?: number;
  status: string;
}

/**
 * 查询系统字典类型列表。
 *
 * @param tenantId 租户业务编码
 * @returns 系统字典类型列表
 */
export function listSystemDictTypes(tenantId: string): Promise<SystemDictType[]> {
  return request<SystemDictType[]>('/system/dicts/types', {
    query: {
      tenantId,
    },
  });
}

export function createSystemDictType(payload: SystemDictTypeSavePayload): Promise<void> {
  return request<void, SystemDictTypeSavePayload>('/system/dicts/types', {
    method: 'POST',
    body: payload,
  });
}

export function updateSystemDictType(typeId: number, payload: SystemDictTypeSavePayload): Promise<void> {
  return request<void, SystemDictTypeSavePayload>(`/system/dicts/types/${typeId}`, {
    method: 'PUT',
    body: payload,
  });
}

export function deleteSystemDictType(typeId: number, tenantId: string): Promise<void> {
  return request<void>(`/system/dicts/types/${typeId}`, {
    method: 'DELETE',
    query: {
      tenantId,
    },
  });
}

/**
 * 查询系统字典项列表。
 *
 * @param tenantId 租户业务编码
 * @param dictCode 字典编码
 * @returns 系统字典项列表
 */
export function listSystemDictItems(tenantId: string, dictCode: string): Promise<SystemDictItem[]> {
  return request<SystemDictItem[]>('/system/dicts/items', {
    query: {
      tenantId,
      dictCode,
    },
  });
}

export function createSystemDictItem(payload: SystemDictItemSavePayload): Promise<void> {
  return request<void, SystemDictItemSavePayload>('/system/dicts/items', {
    method: 'POST',
    body: payload,
  });
}

export function updateSystemDictItem(itemId: number, payload: SystemDictItemSavePayload): Promise<void> {
  return request<void, SystemDictItemSavePayload>(`/system/dicts/items/${itemId}`, {
    method: 'PUT',
    body: payload,
  });
}

export function deleteSystemDictItem(itemId: number, tenantId: string): Promise<void> {
  return request<void>(`/system/dicts/items/${itemId}`, {
    method: 'DELETE',
    query: {
      tenantId,
    },
  });
}
