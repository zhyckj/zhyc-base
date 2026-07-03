/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统编码规则。
 */
export interface SystemCodeRule {
  /** 编码规则编码。 */
  ruleCode: string;
  /** 编码规则名称。 */
  ruleName: string;
  /** 编码前缀。 */
  prefix: string;
  /** 日期格式。 */
  datePattern: string;
  /** 序列号长度。 */
  sequenceLength: number;
  /** 当前序列值。 */
  currentValue: number;
  /** 是否启用。 */
  enabled: boolean;
}

/**
 * 系统编码规则保存参数。
 */
export interface SystemCodeRuleSavePayload extends SystemCodeRule {
  /** 租户业务编码。 */
  tenantId: string;
}

/**
 * 系统编码生成参数。
 */
export interface SystemCodeRuleGeneratePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 编码规则编码。 */
  ruleCode: string;
  /** 业务日期，格式为 yyyy-MM-dd。 */
  businessDate: string;
}

/**
 * 查询系统编码规则列表。
 *
 * @param tenantId 租户业务编码
 * @returns 系统编码规则列表
 */
export function listSystemCodeRules(tenantId: string): Promise<SystemCodeRule[]> {
  return request<SystemCodeRule[]>('/system/code-rules', {
    query: {
      tenantId,
    },
  });
}

/**
 * 保存系统编码规则。
 *
 * @param payload 系统编码规则保存参数
 * @returns 空响应
 */
export function saveSystemCodeRule(payload: SystemCodeRuleSavePayload): Promise<void> {
  return request<void, SystemCodeRuleSavePayload>('/system/code-rules', {
    method: 'PUT',
    body: payload,
  });
}

/**
 * 生成下一个系统编码。
 *
 * @param payload 系统编码生成参数
 * @returns 下一个系统编码
 */
export function generateNextSystemCode(payload: SystemCodeRuleGeneratePayload): Promise<string> {
  return request<string, SystemCodeRuleGeneratePayload>('/system/code-rules/next-code', {
    method: 'POST',
    body: payload,
  });
}
