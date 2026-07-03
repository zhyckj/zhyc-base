/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { spawnSync } from 'node:child_process';

const scriptPath = resolve(process.cwd(), 'scripts/verify-frontend-page-result-contract.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-frontend-page-result-fail-'));
writeFile(failedRoot, 'zhyc-base-vue/src/api/http.ts', `
export interface ApiResult<T> {
  data: T;
}
`);
writeFile(failedRoot, 'zhyc-base-vue/src/api/purchase/request.ts', `
export interface PurRequestPageResponse {
  total: number;
  pageNo: number;
  pageSize: number;
  records: PurRequestStatusResponse[];
}
`);
writeFile(failedRoot, 'zhyc-base-uniapp/src/api/request.ts', `
export interface MobileApiResult<T> {
  data: T;
}
`);
writeFile(failedRoot, 'zhyc-base-uniapp/src/api/message.ts', `
export interface MobileMessagePage {
  total: number;
  pageNo: number;
  pageSize: number;
  records: MobileMessage[];
}
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '前端分页类型未统一时必须触发门禁失败');
assert.match(failedResult.stderr, /PageResult/, '应报告后台管理端缺少统一分页类型');
assert.match(failedResult.stderr, /MobilePageResult/, '应报告移动端缺少统一分页类型');
assert.match(failedResult.stderr, /PurRequestPageResponse/, '应报告后台业务分页类型未复用通用泛型');
assert.match(failedResult.stderr, /MobileMessagePage/, '应报告移动端业务分页类型未复用通用泛型');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-frontend-page-result-pass-'));
writeFile(passedRoot, 'zhyc-base-vue/src/api/http.ts', `
export interface PageResult<T> {
  total: number;
  pageNo: number;
  pageSize: number;
  records: T[];
}
`);
writeFile(passedRoot, 'zhyc-base-vue/src/api/file/object.ts', `
import { type PageResult } from '@/api/http';
export type FileObjectPage = PageResult<FileObjectRecord>;
`);
writeFile(passedRoot, 'zhyc-base-vue/src/api/message/inbox.ts', `
import { type PageResult } from '@/api/http';
export type InboxMessagePage = PageResult<InboxMessage>;
`);
writeFile(passedRoot, 'zhyc-base-vue/src/api/purchase/request.ts', `
import { type PageResult } from '@/api/http';
export type PurRequestPageResponse = PageResult<PurRequestStatusResponse>;
`);
writeFile(passedRoot, 'zhyc-base-vue/src/api/purchase/order.ts', `
import { type PageResult } from '@/api/http';
export type PurOrderPageResponse = PageResult<PurOrderResponse>;
`);
writeFile(passedRoot, 'zhyc-base-uniapp/src/api/request.ts', `
export interface MobilePageResult<T> {
  total: number;
  pageNo: number;
  pageSize: number;
  records: T[];
}
`);
writeFile(passedRoot, 'zhyc-base-uniapp/src/api/message.ts', `
import { type MobilePageResult } from './request';
export type MobileMessagePage = MobilePageResult<MobileMessage>;
`);
writeFile(passedRoot, 'zhyc-base-uniapp/src/api/purchase.ts', `
import { type MobilePageResult } from './request';
export type MobilePurchaseRequestPage = MobilePageResult<MobilePurchaseRequest>;
export type MobilePurchaseOrderPage = MobilePageResult<MobilePurchaseOrder>;
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /前端统一分页响应契约通过/);

/**
 * 写入测试文件。
 *
 * @param root 测试工程根目录
 * @param file 文件相对路径
 * @param content 文件内容
 */
function writeFile(root, file, content) {
  const absolutePath = join(root, file);
  mkdirSync(dirname(absolutePath), { recursive: true });
  writeFileSync(absolutePath, content.trim());
}
