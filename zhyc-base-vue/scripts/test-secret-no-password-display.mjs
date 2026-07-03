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

function assertNotIncludes(file, unexpected, message) {
  if (file.includes(unexpected)) {
    throw new Error(message);
  }
}

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

const view = read('src/views/system/secret/index.vue');
const api = read('src/api/system/secret.ts');

assertNotIncludes(view, "dataIndex: 'secretMask'", '密钥管理列表不能展示密钥掩码字段');
assertNotIncludes(view, "key: 'secretMask'", '密钥管理列表不能保留密钥掩码列标识');
assertNotIncludes(view, '密钥掩码', '密钥管理页面不能出现密钥掩码展示文案');
assertNotIncludes(view, '展示掩码', '密钥管理页面提示不能说明会展示掩码');
assertNotIncludes(api, 'secretMask', '前端系统密钥响应类型不能声明密钥掩码字段');
assertIncludes(view, '密钥列表不展示明文或掩码', '密钥管理页面必须明确说明列表不展示密钥值');

console.log('密钥管理不展示密码门禁通过。');
