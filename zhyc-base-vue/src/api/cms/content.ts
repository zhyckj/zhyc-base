/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 内容栏目。
 */
export interface CmsChannel {
  /** 内容栏目主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 父栏目主键。 */
  parentId?: number;
  /** 栏目编码。 */
  channelCode: string;
  /** 栏目名称。 */
  channelName: string;
  /** 排序号。 */
  sortOrder: number;
  /** 栏目状态。 */
  status: string;
}

/**
 * 内容文章。
 */
export interface CmsContent {
  /** 内容文章主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 栏目编码。 */
  channelCode: string;
  /** 文章标题。 */
  title: string;
  /** 文章摘要。 */
  summary?: string;
  /** 文章正文。 */
  bodyContent?: string;
  /** 文章状态。 */
  status: string;
  /** 作者用户主键。 */
  authorId?: number;
}

/**
 * 内容栏目保存参数。
 */
export interface CmsChannelSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 栏目编码。 */
  channelCode: string;
  /** 栏目名称。 */
  channelName: string;
  /** 父栏目主键。 */
  parentId?: number;
  /** 排序号。 */
  sortOrder: number;
  /** 栏目状态。 */
  status: string;
}

/**
 * 内容文章保存参数。
 */
export interface CmsContentSavePayload {
  /** 内容文章主键；存在时更新租户内已有文章。 */
  id?: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 栏目编码。 */
  channelCode: string;
  /** 文章标题。 */
  title: string;
  /** 文章摘要。 */
  summary?: string;
  /** 文章正文。 */
  bodyContent?: string;
  /** 文章状态。 */
  status?: string;
  /** 作者用户主键。 */
  authorId?: number;
}

/**
 * 查询内容栏目列表。
 *
 * @param status 栏目状态
 * @returns 内容栏目列表
 */
export function listCmsChannels(status?: string): Promise<CmsChannel[]> {
  return request<CmsChannel[]>('/cms/channels', {
    query: {
      status,
    },
  });
}

/**
 * 保存内容栏目。
 *
 * @param payload 内容栏目保存参数
 */
export function saveCmsChannel(payload: CmsChannelSavePayload): Promise<void> {
  return request<void, CmsChannelSavePayload>('/cms/channels', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 查询内容文章列表。
 *
 * @param channelCode 栏目编码
 * @param status 文章状态
 * @returns 内容文章列表
 */
export function listCmsContents(channelCode?: string, status?: string): Promise<CmsContent[]> {
  return request<CmsContent[]>('/cms/contents', {
    query: {
      channelCode,
      status,
    },
  });
}

/**
 * 保存内容文章。
 *
 * @param payload 内容文章保存参数
 */
export function saveCmsContent(payload: CmsContentSavePayload): Promise<void> {
  return request<void, CmsContentSavePayload>('/cms/contents', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 变更内容文章状态。
 *
 * @param id 内容文章主键
 * @param status 文章状态
 */
export function changeCmsContentStatus(id: number, status: string): Promise<void> {
  return request<void, { status: string }>(`/cms/contents/${id}/status`, {
    method: 'POST',
    body: { status },
  });
}
