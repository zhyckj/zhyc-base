/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 消息模板。
 */
export interface MessageTemplate {
  /** 消息模板主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 模板编码。 */
  templateCode: string;
  /** 模板名称。 */
  templateName: string;
  /** 消息通道类型。 */
  channelType: string;
  /** 标题模板。 */
  titleTemplate: string;
  /** 内容模板。 */
  contentTemplate: string;
  /** 模板状态。 */
  status: string;
}

/**
 * 消息模板保存参数。
 */
export interface MessageTemplateSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 模板编码。 */
  templateCode: string;
  /** 模板名称。 */
  templateName: string;
  /** 消息通道类型。 */
  channelType: string;
  /** 标题模板。 */
  titleTemplate: string;
  /** 内容模板。 */
  contentTemplate: string;
  /** 模板状态。 */
  status: string;
}

/**
 * 查询消息模板列表。
 *
 * @returns 消息模板列表
 */
export function listMessageTemplates(): Promise<MessageTemplate[]> {
  return request<MessageTemplate[]>('/message/templates');
}

/**
 * 保存消息模板。
 *
 * @param payload 消息模板保存参数
 * @returns 空响应
 */
export function saveMessageTemplate(payload: MessageTemplateSavePayload): Promise<void> {
  return request<void, MessageTemplateSavePayload>('/message/templates', {
    method: 'PUT',
    body: payload,
  });
}
