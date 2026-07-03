/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');

function read(relativePath) {
  return readFileSync(resolve(root, relativePath), 'utf8');
}

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

function assertNotIncludes(file, unexpected, message) {
  if (file.includes(unexpected)) {
    throw new Error(message);
  }
}

const api = read('src/api/lowcode/datasource.ts');
const view = read('src/views/lowcode/datasource/index.vue');

assertNotIncludes(
  api,
  "listSystemSecretOptions(tenantId, 'db_password', 'enabled')",
  '数据源口令密钥选项不能在前端写死仅查询 db_password 类型',
);
assertIncludes(
  api,
  "listSystemSecretOptions(tenantId, undefined, 'enabled')",
  '数据源口令密钥选项应交由后端返回数据库口令兼容密钥',
);
assertIncludes(
  view,
  'placeholder="请选择启用的数据库口令密钥"',
  '数据源口令引用下拉应使用更准确的口令密钥提示',
);

console.log('数据源口令密钥选项门禁通过。');
