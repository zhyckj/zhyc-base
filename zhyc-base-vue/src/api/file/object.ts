/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request, type PageResult } from '@/api/http';

/**
 * 文件对象。
 */
export interface FileObjectRecord {
  /** 文件对象主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 文件业务编码。 */
  fileCode: string;
  /** 存储配置编码。 */
  storageCode: string;
  /** 原始文件名。 */
  originalName: string;
  /** 文件内容类型。 */
  contentType: string;
  /** 文件大小，单位字节。 */
  fileSize: number;
  /** 存储对象键或相对路径。 */
  objectKey: string;
  /** 文件状态。 */
  fileStatus: string;
  /** 上传人用户 ID。 */
  uploaderId?: number;
  /** 创建时间。 */
  createdAt: string;
}

/** 文件对象分页响应。 */
export type FileObjectPage = PageResult<FileObjectRecord>;

/**
 * 文件对象登记参数。
 */
export interface FileObjectRegisterPayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 存储配置编码。 */
  storageCode: string;
  /** 原始文件名。 */
  originalName: string;
  /** 文件内容类型。 */
  contentType: string;
  /** 文件大小，单位字节。 */
  fileSize: number;
  /** 存储对象键或相对路径。 */
  objectKey: string;
  /** 上传人用户 ID。 */
  uploaderId?: number;
}

/**
 * 文件上传响应。
 */
export interface FileObjectUploadResult {
  /** 文件业务编码。 */
  fileCode: string;
  /** 存储配置编码。 */
  storageCode: string;
  /** 存储对象键或相对路径。 */
  objectKey: string;
  /** 原始文件名。 */
  originalName: string;
  /** 文件内容类型。 */
  contentType: string;
  /** 文件大小，单位字节。 */
  fileSize: number;
}

/**
 * 文件预览响应。
 */
export interface FilePreviewResult {
  /** 文件业务编码。 */
  fileCode: string;
  /** 预览类型。 */
  previewType: string;
  /** 预览访问地址。 */
  previewUrl: string;
  /** 预览结果。 */
  result: string;
}

/**
 * 文件预览渲染响应。
 */
export interface FilePreviewRenderResult {
  /** 文件业务编码。 */
  fileCode: string;
  /** 预览类型。 */
  previewType: string;
  /** 预览访问地址。 */
  previewUrl: string;
  /** 预览渲染结果。 */
  result: string;
}

/**
 * 文件预览日志。
 */
export interface FilePreviewLog {
  /** 预览日志主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 文件业务编码。 */
  fileCode: string;
  /** 预览类型。 */
  previewType: string;
  /** 预览访问地址。 */
  previewUrl: string;
  /** 预览结果。 */
  result: string;
  /** 预览耗时毫秒。 */
  costMs: number;
  /** 创建时间。 */
  createdAt?: string;
}

/**
 * 查询文件对象分页。
 *
 * @param keyword 文件名关键词
 * @returns 文件对象分页
 */
export function listFileObjects(keyword?: string): Promise<FileObjectPage> {
  return request<FileObjectPage>('/file/objects', {
    query: {
      keyword,
      pageNo: 1,
      pageSize: 20,
    },
  });
}

/**
 * 登记文件对象。
 *
 * @param payload 文件对象登记参数
 * @returns 文件业务编码
 */
export function registerFileObject(payload: FileObjectRegisterPayload): Promise<string> {
  return request<string, FileObjectRegisterPayload>('/file/objects', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 上传文件并自动登记文件对象。
 *
 * @param file 上传文件
 * @param storageCode 存储配置编码
 * @returns 文件上传响应
 */
export function uploadFileObject(file: File, storageCode?: string): Promise<FileObjectUploadResult> {
  const formData = new FormData();
  formData.append('file', file);
  return request<FileObjectUploadResult, FormData>('/file/objects/upload', {
    method: 'POST',
    query: { storageCode: storageCode?.trim() || undefined },
    body: formData,
  });
}

/**
 * 创建文件预览。
 *
 * @param fileCode 文件业务编码
 * @param previewType 预览类型
 * @param tenantId 当前租户业务编码
 * @returns 文件预览结果
 */
export function createFilePreview(fileCode: string, previewType: string, tenantId: string): Promise<FilePreviewResult> {
  return request<FilePreviewResult, { tenantId: string; fileCode: string; previewType: string }>('/file/preview', {
    method: 'POST',
    body: {
      tenantId,
      fileCode,
      previewType,
    },
  });
}

/**
 * 渲染文件预览。
 *
 * @param fileCode 文件业务编码
 * @param previewType 预览类型
 * @returns 文件预览渲染结果
 */
export function renderFilePreview(fileCode: string, previewType?: string): Promise<FilePreviewRenderResult> {
  return request<FilePreviewRenderResult>(`/file/preview/render/${encodeURIComponent(fileCode)}`, {
    query: {
      type: previewType,
    },
  });
}

/**
 * 查询文件预览日志。
 *
 * @param fileCode 文件业务编码
 * @returns 文件预览日志列表
 */
export function listFilePreviewLogs(fileCode?: string): Promise<FilePreviewLog[]> {
  return request<FilePreviewLog[]>('/file/preview/logs', {
    query: {
      fileCode,
    },
  });
}
