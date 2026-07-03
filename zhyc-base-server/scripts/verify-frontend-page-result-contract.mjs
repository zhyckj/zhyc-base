/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const rootDir = resolve(process.argv[2] || resolve(process.cwd(), '..'));
const violations = [];

const adminHttpFile = resolve(rootDir, 'zhyc-base-vue/src/api/http.ts');
const adminFiles = [
  {
    file: 'zhyc-base-vue/src/api/file/object.ts',
    snippet: 'export type FileObjectPage = PageResult<FileObjectRecord>',
    message: 'FileObjectPage 必须复用后台管理端 PageResult',
  },
  {
    file: 'zhyc-base-vue/src/api/message/inbox.ts',
    snippet: 'export type InboxMessagePage = PageResult<InboxMessage>',
    message: 'InboxMessagePage 必须复用后台管理端 PageResult',
  },
  {
    file: 'zhyc-base-vue/src/api/purchase/request.ts',
    snippet: 'export type PurRequestPageResponse = PageResult<PurRequestStatusResponse>',
    message: 'PurRequestPageResponse 必须复用后台管理端 PageResult',
  },
  {
    file: 'zhyc-base-vue/src/api/purchase/order.ts',
    snippet: 'export type PurOrderPageResponse = PageResult<PurOrderResponse>',
    message: 'PurOrderPageResponse 必须复用后台管理端 PageResult',
  },
];

const mobileRequestFile = resolve(rootDir, 'zhyc-base-uniapp/src/api/request.ts');
const mobileFiles = [
  {
    file: 'zhyc-base-uniapp/src/api/message.ts',
    snippet: 'export type MobileMessagePage = MobilePageResult<MobileMessage>',
    message: 'MobileMessagePage 必须复用移动端 MobilePageResult',
  },
  {
    file: 'zhyc-base-uniapp/src/api/purchase.ts',
    snippet: 'export type MobilePurchaseRequestPage = MobilePageResult<MobilePurchaseRequest>',
    message: 'MobilePurchaseRequestPage 必须复用移动端 MobilePageResult',
  },
  {
    file: 'zhyc-base-uniapp/src/api/purchase.ts',
    snippet: 'export type MobilePurchaseOrderPage = MobilePageResult<MobilePurchaseOrder>',
    message: 'MobilePurchaseOrderPage 必须复用移动端 MobilePageResult',
  },
];

requireFile(adminHttpFile, '缺少后台管理端请求基础模块');
requireFile(mobileRequestFile, '缺少移动端请求基础模块');

if (existsSync(adminHttpFile)) {
  const content = readFileSync(adminHttpFile, 'utf8');
  requireSnippet(content, 'interface PageResult<T>', '后台管理端必须定义统一分页类型 PageResult');
  requireSnippet(content, 'records: T[]', '后台管理端 PageResult 必须包含泛型 records 字段');
}

if (existsSync(mobileRequestFile)) {
  const content = readFileSync(mobileRequestFile, 'utf8');
  requireSnippet(content, 'interface MobilePageResult<T>', '移动端必须定义统一分页类型 MobilePageResult');
  requireSnippet(content, 'records: T[]', '移动端 MobilePageResult 必须包含泛型 records 字段');
}

for (const item of [...adminFiles, ...mobileFiles]) {
  const absolutePath = resolve(rootDir, item.file);
  requireFile(absolutePath, `缺少分页接口文件：${item.file}`);
  if (existsSync(absolutePath)) {
    requireSnippet(readFileSync(absolutePath, 'utf8'), item.snippet, item.message);
  }
}

if (violations.length > 0) {
  console.error('前端统一分页响应契约失败。分页类型必须复用通用泛型：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('前端统一分页响应契约通过。');

/**
 * 校验文件存在。
 *
 * @param file 文件路径
 * @param message 缺失提示
 */
function requireFile(file, message) {
  if (!existsSync(file)) {
    violations.push(`${file} -> ${message}`);
  }
}

/**
 * 校验内容片段存在。
 *
 * @param content 文件内容
 * @param snippet 必须存在的片段
 * @param message 缺失提示
 */
function requireSnippet(content, snippet, message) {
  if (!content.includes(snippet)) {
    violations.push(`${snippet} -> ${message}`);
  }
}
