/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { mobileRequest, type MobilePageResult } from './request';

/**
 * 移动端消息记录。
 */
export interface MobileMessage {
  /** 消息编码。 */
  messageCode: string;
  /** 消息标题。 */
  title: string;
  /** 消息内容。 */
  content: string;
  /** 消息类型。 */
  messageType: string;
  /** 是否已读。 */
  readFlag: boolean;
  /** 创建时间。 */
  createdAt: string;
}

/**
 * 移动端消息查询参数。
 */
export interface MobileMessageQuery {
  /** 是否已读。 */
  readFlag?: boolean;
  /** 当前页码。 */
  pageNo: number;
  /** 每页记录数。 */
  pageSize: number;
}

/** 移动端消息分页响应。 */
export type MobileMessagePage = MobilePageResult<MobileMessage>;

/**
 * 分页查询移动端消息。
 *
 * @param query 消息查询参数
 * @returns 消息分页数据
 */
export function listMobileMessages(query: MobileMessageQuery): Promise<MobileMessagePage> {
  return mobileRequest<MobileMessagePage>('/message/inbox', {
    query: {
      readFlag: query.readFlag,
      pageNo: query.pageNo,
      pageSize: query.pageSize,
    },
  });
}

/**
 * 标记移动端消息为已读。
 *
 * @param messageId 消息 ID
 */
export function markMobileMessageRead(messageCode: string): Promise<void> {
  return mobileRequest<void>(`/message/inbox/${messageCode}/read`, {
    method: 'PATCH',
  });
}
