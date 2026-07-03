/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const aiView = readFileSync(resolve(root, 'src/views/ai/core/index.vue'), 'utf8');

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

assertIncludes(
  aiView,
  'listSystemSecretOptions',
  'AI 供应商表单必须加载系统密钥选项，支持密钥引用下拉选择。',
);
assertIncludes(
  aiView,
  'buildSystemSecretSelectOptions',
  'AI 供应商表单必须复用系统密钥下拉转换函数，保持 secret:xxx 引用格式一致。',
);
assertIncludes(
  aiView,
  'providerSecretOptions',
  'AI 供应商表单必须维护供应商密钥候选项。',
);
assertIncludes(
  aiView,
  'providerSecretSelectOptions',
  'AI 供应商表单必须把当前引用合并到可选项，避免编辑态丢失展示。',
);
assertIncludes(
  aiView,
  ':options="providerSecretSelectOptions"',
  '密钥引用控件必须绑定可选密钥列表。',
);
assertIncludes(
  aiView,
  ':loading="providerSecretLoading"',
  '密钥引用控件必须展示加载态，避免用户误以为无密钥。',
);
assertIncludes(
  aiView,
  'show-search',
  '密钥引用控件必须支持搜索，方便密钥较多时选择。',
);
assertIncludes(
  aiView,
  'placeholder="请选择启用的系统密钥"',
  '密钥引用控件必须给出可选择的明确提示。',
);
assertNotIncludes(
  aiView,
  '<a-input v-model:value="providerCommand.secretRef" placeholder="secret:openai-api-key" />',
  '密钥引用不能继续使用纯文本输入框。',
);

console.log('AI 供应商密钥引用选择门禁通过。');
