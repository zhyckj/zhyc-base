/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统密码策略。
 */
export interface SystemPasswordPolicy {
  /** 策略编码。 */
  policyCode: string;
  /** 策略名称。 */
  policyName: string;
  /** 密码最小长度。 */
  minLength: number;
  /** 是否要求包含大写字母。 */
  requireUppercase: boolean;
  /** 是否要求包含小写字母。 */
  requireLowercase: boolean;
  /** 是否要求包含数字。 */
  requireDigit: boolean;
  /** 是否要求包含特殊字符。 */
  requireSpecial: boolean;
  /** 密码有效天数。 */
  expireDays: number;
  /** 历史密码记忆次数。 */
  historyCount: number;
  /** 最大连续失败次数。 */
  maxRetryCount: number;
  /** 账号锁定分钟数。 */
  lockMinutes: number;
  /** 是否启用。 */
  enabled: boolean;
}

/**
 * 系统密码策略保存参数。
 */
export interface SystemPasswordPolicySavePayload extends SystemPasswordPolicy {
  /** 租户业务编码。 */
  tenantId: string;
}

/**
 * 密码策略校验参数。
 */
export interface SystemPasswordPolicyValidatePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 待校验密码。 */
  password: string;
}

/**
 * 密码历史策略校验参数。
 */
export interface SystemPasswordHistoryValidatePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 新密码哈希值；不得传输密码明文。 */
  passwordHash: string;
  /** 最近密码哈希列表，按从新到旧排序。 */
  recentPasswordHashes: string[];
}

/**
 * 密码策略校验结果。
 */
export interface SystemPasswordPolicyValidateResult {
  /** 是否通过策略校验。 */
  valid: boolean;
  /** 未通过原因。 */
  messages: string[];
}

/**
 * 查询租户密码策略。
 *
 * @param tenantId 租户业务编码
 * @returns 系统密码策略
 */
export function getSystemPasswordPolicy(tenantId: string): Promise<SystemPasswordPolicy> {
  return request<SystemPasswordPolicy>('/system/password-policies', {
    query: {
      tenantId,
    },
  });
}

/**
 * 保存租户密码策略。
 *
 * @param payload 系统密码策略保存参数
 * @returns 空响应
 */
export function saveSystemPasswordPolicy(payload: SystemPasswordPolicySavePayload): Promise<void> {
  return request<void, SystemPasswordPolicySavePayload>('/system/password-policies', {
    method: 'PUT',
    body: payload,
  });
}

/**
 * 校验密码是否满足租户密码策略。
 *
 * @param payload 密码策略校验参数
 * @returns 密码策略校验结果
 */
export function validateSystemPasswordPolicy(
  payload: SystemPasswordPolicyValidatePayload,
): Promise<SystemPasswordPolicyValidateResult> {
  return request<SystemPasswordPolicyValidateResult, SystemPasswordPolicyValidatePayload>(
    '/system/password-policies/validate',
    {
      method: 'POST',
      body: payload,
    },
  );
}

/**
 * 校验新密码哈希是否违反历史密码复用策略。
 *
 * @param payload 密码历史策略校验参数
 * @returns 密码策略校验结果
 */
export function validateSystemPasswordHistory(
  payload: SystemPasswordHistoryValidatePayload,
): Promise<SystemPasswordPolicyValidateResult> {
  return request<SystemPasswordPolicyValidateResult, SystemPasswordHistoryValidatePayload>(
    '/system/password-policies/validate-history',
    {
      method: 'POST',
      body: payload,
    },
  );
}
