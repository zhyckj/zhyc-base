/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const rootDir = resolve(process.argv[2] || process.cwd());
const violations = [];

const pageResultFile = resolve(rootDir, 'zhyc-common/src/main/java/com/zhyc/common/api/PageResult.java');
const fileObjectServiceFile = resolve(rootDir,
  'zhyc-module-file/src/main/java/com/zhyc/file/object/service/FileObjectService.java');
const fileObjectControllerFile = resolve(rootDir,
  'zhyc-module-file/src/main/java/com/zhyc/file/object/controller/FileObjectController.java');
const fileObjectPrivatePageFile = resolve(rootDir,
  'zhyc-module-file/src/main/java/com/zhyc/file/object/service/FileObjectPageResponse.java');
const msgMessageServiceFile = resolve(rootDir,
  'zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/MsgMessageService.java');
const msgMessageControllerFile = resolve(rootDir,
  'zhyc-module-message/src/main/java/com/zhyc/message/inbox/controller/MsgMessageController.java');
const msgMessagePrivatePageFile = resolve(rootDir,
  'zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/MsgMessagePageResponse.java');
const purRequestServiceFile = resolve(rootDir,
  'zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/service/PurRequestCommandService.java');
const purRequestControllerFile = resolve(rootDir,
  'zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/controller/PurRequestAdminController.java');
const purRequestPrivatePageFile = resolve(rootDir,
  'zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/service/PurRequestPageResponse.java');
const purOrderServiceFile = resolve(rootDir,
  'zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/service/PurOrderCommandService.java');
const purOrderControllerFile = resolve(rootDir,
  'zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller/PurOrderAdminController.java');
const purOrderPrivatePageFile = resolve(rootDir,
  'zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/service/PurOrderPageResponse.java');

requireFile(pageResultFile, '缺少公共分页响应 PageResult');
requireFile(fileObjectServiceFile, '缺少文件对象服务接口');
requireFile(fileObjectControllerFile, '缺少文件对象控制器');
requireFile(msgMessageServiceFile, '缺少消息服务接口');
requireFile(msgMessageControllerFile, '缺少消息控制器');
requireFile(purRequestServiceFile, '缺少采购申请命令服务接口');
requireFile(purRequestControllerFile, '缺少采购申请后台控制器');
requireFile(purOrderServiceFile, '缺少采购订单命令服务接口');
requireFile(purOrderControllerFile, '缺少采购订单后台控制器');

if (existsSync(pageResultFile)) {
  const content = readFileSync(pageResultFile, 'utf8');
  requireSnippet(content, 'PageResult<T>', 'PageResult 必须是泛型分页响应');
  requireSnippet(content, 'MAX_PAGE_SIZE', 'PageResult 必须限制最大页大小');
  requireSnippet(content, 'private final long total', 'PageResult 必须包含 total 字段');
  requireSnippet(content, 'private final int pageNo', 'PageResult 必须包含 pageNo 字段');
  requireSnippet(content, 'private final int pageSize', 'PageResult 必须包含 pageSize 字段');
  requireSnippet(content, 'private final List<T> records', 'PageResult 必须包含 records 字段');
  requireSnippet(content, 'List.copyOf(records)', 'PageResult 必须复制记录列表，避免响应被外部修改');
}

if (existsSync(fileObjectServiceFile)) {
  const content = readFileSync(fileObjectServiceFile, 'utf8');
  requireSnippet(content, 'PageResult<FileObjectResponse> listFiles', 'FileObjectService 分页接口必须返回 PageResult');
}

if (existsSync(fileObjectControllerFile)) {
  const content = readFileSync(fileObjectControllerFile, 'utf8');
  requireSnippet(content, 'ApiResult<PageResult<FileObjectResponse>>', 'FileObjectController 必须返回 ApiResult<PageResult<...>>');
}

if (existsSync(fileObjectPrivatePageFile)) {
  violations.push('zhyc-module-file/src/main/java/com/zhyc/file/object/service/FileObjectPageResponse.java -> FileObjectPageResponse 已被 PageResult 替代，不允许残留模块私有分页响应');
}

if (existsSync(msgMessageServiceFile)) {
  const content = readFileSync(msgMessageServiceFile, 'utf8');
  requireSnippet(content, 'PageResult<MsgMessageResponse> listMessages', 'MsgMessageService 分页接口必须返回 PageResult');
}

if (existsSync(msgMessageControllerFile)) {
  const content = readFileSync(msgMessageControllerFile, 'utf8');
  requireSnippet(content, 'ApiResult<PageResult<MsgMessageResponse>>', 'MsgMessageController 必须返回 ApiResult<PageResult<...>>');
}

if (existsSync(msgMessagePrivatePageFile)) {
  violations.push('zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/MsgMessagePageResponse.java -> MsgMessagePageResponse 已被 PageResult 替代，不允许残留模块私有分页响应');
}

if (existsSync(purRequestServiceFile)) {
  const content = readFileSync(purRequestServiceFile, 'utf8');
  requireSnippet(content, 'PageResult<PurRequestStatusResponse> list', 'PurRequestCommandService 分页接口必须返回 PageResult');
}

if (existsSync(purRequestControllerFile)) {
  const content = readFileSync(purRequestControllerFile, 'utf8');
  requireSnippet(content, 'ApiResult<PageResult<PurRequestStatusResponse>>', 'PurRequestAdminController 必须返回 ApiResult<PageResult<...>>');
}

if (existsSync(purRequestPrivatePageFile)) {
  violations.push('zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/service/PurRequestPageResponse.java -> PurRequestPageResponse 已被 PageResult 替代，不允许残留模块私有分页响应');
}

if (existsSync(purOrderServiceFile)) {
  const content = readFileSync(purOrderServiceFile, 'utf8');
  requireSnippet(content, 'PageResult<PurOrderResponse> list', 'PurOrderCommandService 分页接口必须返回 PageResult');
}

if (existsSync(purOrderControllerFile)) {
  const content = readFileSync(purOrderControllerFile, 'utf8');
  requireSnippet(content, 'ApiResult<PageResult<PurOrderResponse>>', 'PurOrderAdminController 必须返回 ApiResult<PageResult<...>>');
}

if (existsSync(purOrderPrivatePageFile)) {
  violations.push('zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/service/PurOrderPageResponse.java -> PurOrderPageResponse 已被 PageResult 替代，不允许残留模块私有分页响应');
}

if (violations.length > 0) {
  console.error('统一分页响应契约失败。分页接口必须使用 zhyc-common 的 PageResult：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('统一分页响应契约通过。');

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
