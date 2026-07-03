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

const view = read('src/views/system/secret/index.vue');
const api = read('src/api/system/secret.ts');

assertIncludes(view, '<a-date-picker', '密钥到期时间必须使用日期时间控件');
assertIncludes(view, 'value-format="YYYY-MM-DDTHH:mm:ss"', '密钥到期时间必须按后端 LocalDateTime 格式提交');
assertIncludes(view, 'format="YYYY-MM-DD HH:mm:ss"', '密钥到期时间必须使用易读展示格式');
assertNotIncludes(view, '<a-input v-model:value="formState.expireAt"', '新增/编辑密钥到期时间不能继续使用普通输入框');
assertNotIncludes(view, '<a-input v-model:value="rotateState.expireAt"', '轮换密钥到期时间不能继续使用普通输入框');

assertIncludes(api, 'secretPlaintext: string;', '创建密钥请求必须向后端提交 secretPlaintext');
assertIncludes(api, 'secretPlaintext?: string;', '更新密钥请求必须向后端提交可选 secretPlaintext');
assertIncludes(api, 'secretPlaintext: string;', '轮换密钥请求必须向后端提交 secretPlaintext');
assertNotIncludes(api, 'secretCipher: string;', '前端 API 请求不能继续使用后端不接收的 secretCipher 字段');
assertNotIncludes(view, 'secretCipher', '密钥管理页不能继续使用后端不接收的 secretCipher 字段');

console.log('密钥表单日期控件和提交字段门禁通过。');
