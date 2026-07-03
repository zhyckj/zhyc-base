/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const secretApi = readFileSync(resolve(root, 'src/api/system/secret.ts'), 'utf8');
const secretView = readFileSync(resolve(root, 'src/views/system/secret/index.vue'), 'utf8');
const aiView = readFileSync(resolve(root, 'src/views/ai/core/index.vue'), 'utf8');

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

function assertMatches(file, pattern, message) {
  if (!pattern.test(file)) {
    throw new Error(message);
  }
}

assertMatches(
  secretApi,
  /export interface SystemSecretUpdateRequest \{[\s\S]*?secretCode: string;[\s\S]*?\}/,
  '编辑密钥请求类型必须包含 secretCode，避免后端更新校验拿不到密钥编码。',
);
assertIncludes(
  secretView,
  'secretCode: formState.secretCode.trim(),',
  '编辑密钥提交时必须携带 secretCode，修复编辑保存提示“密钥编码不能为空”的问题。',
);
assertIncludes(
  secretView,
  "{ label: 'API 密钥', value: 'api_secret' }",
  '密钥管理必须保留 API 密钥类型，供 AI 供应商引用。',
);
assertIncludes(
  aiView,
  "await listSystemSecretOptions(currentTenant, 'api_secret', 'enabled')",
  'AI 供应商密钥下拉必须按 API 密钥类型加载当前租户启用密钥，避免新增密钥后无法选择。',
);
assertIncludes(
  secretApi,
  'secretRef: string;',
  '系统密钥选项必须返回 secretRef，供供应商保存 secret:xxx 引用。',
);

console.log('密钥编辑与 AI 供应商密钥选项门禁通过。');
