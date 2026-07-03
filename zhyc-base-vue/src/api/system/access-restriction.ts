/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统访问限制。
 */
export interface SystemAccessRestriction {
  /** 数据库主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 限制类型，例如 ip、account、device。 */
  restrictionType: string;
  /** 规则值，例如 IP、账号或设备标识。 */
  ruleValue: string;
  /** 生效动作，例如 allow 或 deny。 */
  effect: string;
  /** 生效开始时间。 */
  startAt?: string;
  /** 生效结束时间。 */
  endAt?: string;
}

/**
 * 系统访问限制保存参数。
 */
export interface SystemAccessRestrictionSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 限制类型。 */
  restrictionType: string;
  /** 规则值。 */
  ruleValue: string;
  /** 生效动作。 */
  effect: string;
  /** 生效开始时间。 */
  startAt?: string;
  /** 生效结束时间。 */
  endAt?: string;
}

/**
 * 系统访问限制判定参数。
 */
export interface SystemAccessRestrictionEvaluatePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 限制类型，例如 ip、account、device。 */
  restrictionType: string;
  /** 待判定访问标识，例如 IP、账号或设备标识。 */
  accessValue: string;
}

/**
 * 系统访问限制判定结果。
 */
export interface SystemAccessRestrictionEvaluationResult {
  /** 是否允许访问。 */
  allowed: boolean;
  /** 命中的生效动作。 */
  effect: string;
  /** 命中的规则值。 */
  matchedRuleValue?: string;
}

/**
 * 查询当前生效的系统访问限制。
 *
 * @param tenantId 租户业务编码
 * @param restrictionType 限制类型
 * @returns 系统访问限制列表
 */
export function listSystemAccessRestrictions(
  tenantId: string,
  restrictionType: string,
): Promise<SystemAccessRestriction[]> {
  return request<SystemAccessRestriction[]>('/system/access-restrictions', {
    query: {
      tenantId,
      restrictionType,
    },
  });
}

/**
 * 保存系统访问限制。
 *
 * @param payload 系统访问限制保存参数
 * @returns 空响应
 */
export function saveSystemAccessRestriction(payload: SystemAccessRestrictionSavePayload): Promise<void> {
  return request<void, SystemAccessRestrictionSavePayload>('/system/access-restrictions', {
    method: 'PUT',
    body: payload,
  });
}

/**
 * 判定指定访问标识是否允许访问。
 *
 * @param payload 系统访问限制判定参数
 * @returns 系统访问限制判定结果
 */
export function evaluateSystemAccessRestriction(
  payload: SystemAccessRestrictionEvaluatePayload,
): Promise<SystemAccessRestrictionEvaluationResult> {
  return request<SystemAccessRestrictionEvaluationResult, SystemAccessRestrictionEvaluatePayload>(
    '/system/access-restrictions/evaluate',
    {
      method: 'POST',
      body: payload,
    },
  );
}
