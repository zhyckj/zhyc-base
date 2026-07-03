/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 安全防护总览。
 */
export interface SystemSecurityOverview {
  /** 统计日期，格式 yyyyMMdd。 */
  statDate: string;
  /** 今日请求来源数。 */
  todaySourceCount: number;
  /** 单 IP 最高请求次数。 */
  maxIpRequestCount: number;
  /** 违规 IP 数量。 */
  violationIpCount: number;
  /** 当前封禁 IP 数量。 */
  blockedIpCount: number;
}

/**
 * 安全防护策略。
 */
export interface SystemSecurityPolicy {
  /** 数据库主键。 */
  id?: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 策略编码。 */
  policyCode: string;
  /** 策略名称。 */
  policyName: string;
  /** 防护范围。 */
  protectionScope: string;
  /** 目标匹配表达式。 */
  targetPattern: string;
  /** 阈值次数。 */
  thresholdLimit: number;
  /** 时间窗口秒数。 */
  windowSeconds: number;
  /** 处置动作。 */
  action: string;
  /** 自动封禁秒数。 */
  blockSeconds?: number;
  /** 策略状态。 */
  status: string;
}

/**
 * 安全事件。
 */
export interface SystemSecurityEvent {
  /** 数据库主键。 */
  id: number;
  /** 事件类型。 */
  eventType: string;
  /** 事件等级。 */
  eventLevel: string;
  /** 来源 IP。 */
  sourceIp?: string;
  /** 用户账号。 */
  username?: string;
  /** 请求路径。 */
  requestPath?: string;
  /** HTTP 方法。 */
  httpMethod?: string;
  /** 处置动作。 */
  action: string;
  /** 处置结果。 */
  result: string;
  /** 事件描述。 */
  message?: string;
  /** 发生时间。 */
  occurredAt: string;
}

/**
 * 安全排行。
 */
export interface SystemSecurityRank {
  /** 排行名称。 */
  name: string;
  /** 请求次数。 */
  requestCount: number;
}

/**
 * IP 封禁参数。
 */
export interface SystemSecurityIpBlockPayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** IP、IPv6 或 CIDR。 */
  ipValue: string;
  /** 封禁类型。 */
  blockType?: string;
  /** 封禁原因。 */
  reason?: string;
  /** 生效开始时间。 */
  startAt?: string;
  /** 生效结束时间。 */
  endAt?: string;
}

/**
 * 查询安全防护总览。
 *
 * @param tenantId 租户业务编码
 * @returns 安全防护总览
 */
export function getSystemSecurityOverview(tenantId: string): Promise<SystemSecurityOverview> {
  return request<SystemSecurityOverview>('/system/security-protection/overview', {
    query: { tenantId },
  });
}

/**
 * 查询安全防护策略。
 *
 * @param tenantId 租户业务编码
 * @returns 安全防护策略列表
 */
export function listSystemSecurityPolicies(tenantId: string): Promise<SystemSecurityPolicy[]> {
  return request<SystemSecurityPolicy[]>('/system/security-protection/policies', {
    query: { tenantId },
  });
}

/**
 * 保存安全防护策略。
 *
 * @param payload 安全防护策略
 * @returns 空响应
 */
export function saveSystemSecurityPolicy(payload: SystemSecurityPolicy): Promise<void> {
  return request<void, SystemSecurityPolicy>('/system/security-protection/policies', {
    method: 'PUT',
    body: payload,
  });
}

/**
 * 查询最近安全事件。
 *
 * @param tenantId 租户业务编码
 * @param limit 返回数量
 * @returns 最近安全事件列表
 */
export function listSystemSecurityEvents(tenantId: string, limit = 20): Promise<SystemSecurityEvent[]> {
  return request<SystemSecurityEvent[]>('/system/security-protection/events', {
    query: { tenantId, limit },
  });
}

/**
 * 查询来源 IP 请求排行。
 *
 * @param tenantId 租户业务编码
 * @param limit 返回数量
 * @returns 来源 IP 请求排行
 */
export function listSystemSecurityIpRanking(tenantId: string, limit = 10): Promise<SystemSecurityRank[]> {
  return request<SystemSecurityRank[]>('/system/security-protection/ip-ranking', {
    query: { tenantId, limit },
  });
}

/**
 * 查询接口访问排行。
 *
 * @param tenantId 租户业务编码
 * @param limit 返回数量
 * @returns 接口访问排行
 */
export function listSystemSecurityApiRanking(tenantId: string, limit = 10): Promise<SystemSecurityRank[]> {
  return request<SystemSecurityRank[]>('/system/security-protection/api-ranking', {
    query: { tenantId, limit },
  });
}

/**
 * 封禁 IP。
 *
 * @param payload IP 封禁参数
 * @returns 空响应
 */
export function blockSystemSecurityIp(payload: SystemSecurityIpBlockPayload): Promise<void> {
  return request<void, SystemSecurityIpBlockPayload>('/system/security-protection/ip-blocks', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 解封 IP。
 *
 * @param tenantId 租户业务编码
 * @param ipValue IP、IPv6 或 CIDR
 * @returns 空响应
 */
export function unblockSystemSecurityIp(tenantId: string, ipValue: string): Promise<void> {
  return request<void, { tenantId: string; ipValue: string }>('/system/security-protection/ip-blocks/unblock', {
    method: 'POST',
    body: {
      tenantId,
      ipValue,
    },
  });
}
