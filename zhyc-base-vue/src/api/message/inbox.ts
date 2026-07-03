/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request, type PageResult } from '@/api/http';

/**
 * 站内消息。
 */
export interface InboxMessage {
  /** 站内消息主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 消息编码。 */
  messageCode: string;
  /** 接收人用户 ID。 */
  receiverId: number;
  /** 接收人名称。 */
  receiverName?: string;
  /** 消息类型。 */
  messageType: string;
  /** 消息标题。 */
  title: string;
  /** 消息内容。 */
  content: string;
  /** 是否已读。 */
  readFlag: boolean;
  /** 阅读时间。 */
  readAt?: string;
  /** 创建时间。 */
  createdAt: string;
}

/** 站内消息分页响应。 */
export type InboxMessagePage = PageResult<InboxMessage>;

/**
 * 站内消息发送参数。
 */
export interface InboxMessageSendPayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 接收人用户 ID。 */
  receiverId: number;
  /** 接收人名称。 */
  receiverName?: string;
  /** 消息类型。 */
  messageType: string;
  /** 消息标题。 */
  title: string;
  /** 消息内容。 */
  content: string;
}

/**
 * 站内消息模板发送参数。
 */
export interface InboxMessageTemplateSendPayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 消息模板编码。 */
  templateCode: string;
  /** 接收人用户 ID。 */
  receiverId: number;
  /** 接收人名称。 */
  receiverName?: string;
  /** 消息类型。 */
  messageType: string;
  /** 模板变量，key 对应模板中的 ${变量名}。 */
  variables: Record<string, string>;
}

/**
 * 查询站内消息分页。
 *
 * @param readFlag 已读状态，空值表示全部
 * @returns 站内消息分页
 */
export function listInboxMessages(readFlag: boolean | undefined): Promise<InboxMessagePage> {
  return request<InboxMessagePage>('/message/inbox', {
    query: {
      readFlag,
      pageNo: 1,
      pageSize: 20,
    },
  });
}

/**
 * 发送站内消息。
 *
 * @param payload 站内消息发送参数
 * @returns 消息编码
 */
export function sendInboxMessage(payload: InboxMessageSendPayload): Promise<string> {
  return request<string, InboxMessageSendPayload>('/message/inbox', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 按启用模板发送站内消息。
 *
 * @param payload 站内消息模板发送参数
 * @returns 消息编码
 */
export function sendInboxMessageByTemplate(payload: InboxMessageTemplateSendPayload): Promise<string> {
  return request<string, InboxMessageTemplateSendPayload>('/message/inbox/template', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 标记站内消息已读。
 *
 * @param messageCode 消息编码
 * @returns 空响应
 */
export function markInboxMessageRead(messageCode: string): Promise<void> {
  return request<void>(`/message/inbox/${messageCode}/read`, {
    method: 'PATCH',
  });
}
