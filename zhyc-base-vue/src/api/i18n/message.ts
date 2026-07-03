/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 国际化词条。
 */
export interface I18nMessage {
  /** 词条主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 语言标识。 */
  locale: string;
  /** 词条键。 */
  messageKey: string;
  /** 词条值。 */
  messageValue: string;
  /** 词条状态。 */
  status: string;
}

/**
 * 国际化词条保存参数。
 */
export interface I18nMessageSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 语言标识。 */
  locale: string;
  /** 词条键。 */
  messageKey: string;
  /** 词条值。 */
  messageValue: string;
  /** 词条状态。 */
  status?: string;
}

/**
 * 国际化词条批量解析参数。
 */
export interface I18nResolvePayload {
  /** 语言标识。 */
  locale: string;
  /** 词条键与默认文案映射。 */
  defaults: Record<string, string>;
}

/**
 * 国际化词条批量解析结果。
 */
export interface I18nResolveResult {
  /** 语言标识。 */
  locale: string;
  /** 词条键与解析文案映射。 */
  messages: Record<string, string>;
}

/**
 * 查询国际化词条列表。
 *
 * @param locale 语言标识
 * @param status 词条状态
 * @returns 国际化词条列表
 */
export function listI18nMessages(locale?: string, status?: string): Promise<I18nMessage[]> {
  return request<I18nMessage[]>('/i18n/messages', {
    query: {
      locale,
      status,
    },
  });
}

/**
 * 保存国际化词条。
 *
 * @param payload 国际化词条保存参数
 */
export function saveI18nMessage(payload: I18nMessageSavePayload): Promise<void> {
  return request<void, I18nMessageSavePayload>('/i18n/messages', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 批量解析国际化词条。
 *
 * @param payload 国际化词条批量解析参数
 * @returns 国际化词条批量解析结果
 */
export function resolveI18nMessages(payload: I18nResolvePayload): Promise<I18nResolveResult> {
  return request<I18nResolveResult, I18nResolvePayload>('/i18n/messages/resolve', {
    method: 'POST',
    body: payload,
  });
}
