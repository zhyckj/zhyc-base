/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 低代码数据库方言能力响应。
 */
export interface LowcodeDialectCapabilitiesResponse {
  /** 已注册的 DDL 生成器方言编码清单。 */
  ddlDialectCodes: string[];
  /** 已注册的字段类型映射方言编码清单。 */
  fieldTypeDialectCodes: string[];
  /** 已注册的分页方言编码清单。 */
  paginationDialectCodes: string[];
}

/** 低代码默认数据库方言能力；后端能力接口异常时用于保持数据源和生成入口可选择主流数据库。 */
export const DEFAULT_LOWCODE_DIALECT_CAPABILITIES: LowcodeDialectCapabilitiesResponse = {
  ddlDialectCodes: ['mysql', 'postgresql', 'oracle', 'sqlserver', 'dm'],
  fieldTypeDialectCodes: ['mysql', 'postgresql', 'oracle', 'sqlserver', 'dm'],
  paginationDialectCodes: ['mysql', 'postgresql', 'oracle', 'sqlserver', 'dm'],
};

/**
 * 查询当前平台支持的低代码数据库方言能力。
 */
export function listLowcodeDialectCapabilities(): Promise<LowcodeDialectCapabilitiesResponse> {
  return request<LowcodeDialectCapabilitiesResponse>('/lowcode/dialects/capabilities');
}

/**
 * 归一化低代码数据库方言编码。
 *
 * @param code 数据库方言编码
 * @returns 去除首尾空白并转为小写后的编码
 */
export function normalizeLowcodeDialectCode(code: string): string {
  return code.trim().toLowerCase();
}

/**
 * 计算同时具备 DDL、字段映射和分页能力的数据库方言编码。
 *
 * @param capabilities 数据库方言能力响应
 * @returns 三类能力交集方言编码
 */
export function buildSupportedLowcodeDialectCodes(
  capabilities: LowcodeDialectCapabilitiesResponse,
): string[] {
  const ddlDialectCodes = normalizeDialectCodes(capabilities.ddlDialectCodes);
  const fieldTypeDialectCodes = new Set(normalizeDialectCodes(capabilities.fieldTypeDialectCodes));
  const paginationDialectCodes = new Set(normalizeDialectCodes(capabilities.paginationDialectCodes));
  return Array.from(new Set(ddlDialectCodes
    .filter((code) => fieldTypeDialectCodes.has(code))
    .filter((code) => paginationDialectCodes.has(code))));
}

/**
 * 判断指定方言是否同时具备 DDL、字段映射和分页能力。
 *
 * @param capabilities 数据库方言能力响应
 * @param dialectCode 待判断数据库方言编码
 * @returns 三类能力均支持该方言时返回 true
 */
export function isLowcodeDialectFullySupported(
  capabilities: LowcodeDialectCapabilitiesResponse,
  dialectCode: string,
): boolean {
  const normalized = normalizeLowcodeDialectCode(dialectCode);
  return buildSupportedLowcodeDialectCodes(capabilities).includes(normalized);
}

/**
 * 格式化低代码数据库方言展示名称。
 *
 * @param code 数据库方言编码
 * @returns 面向后台页面的方言展示名称
 */
export function formatLowcodeDialectLabel(code: string): string {
  const normalized = normalizeLowcodeDialectCode(code);
  const dialectLabels: Record<string, string> = {
    mysql: 'MySQL',
    postgresql: 'PostgreSQL',
    oracle: 'Oracle',
    sqlserver: 'SQL Server',
    dm: '达梦数据库',
  };
  return dialectLabels[normalized] ?? code;
}

/**
 * 批量归一化方言编码并过滤空值。
 *
 * @param codes 数据库方言编码清单
 * @returns 已归一化的方言编码清单
 */
function normalizeDialectCodes(codes: string[]): string[] {
  return codes
    .map((code) => normalizeLowcodeDialectCode(code))
    .filter(Boolean);
}
