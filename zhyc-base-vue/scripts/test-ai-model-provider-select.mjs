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
const aiApi = readFileSync(resolve(root, 'src/api/ai/core.ts'), 'utf8');

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

assertIncludes(aiApi, 'id: number;', 'AI 供应商响应必须包含内部主键，供模型配置提交 providerId 使用。');
assertIncludes(aiApi, 'providerId: number;', 'AI 模型响应必须包含供应商主键，供编辑态回填选择项。');
assertIncludes(aiView, 'label="供应商"', '模型配置表单应展示业务语义为供应商，而不是供应商主键。');
assertIncludes(aiView, ':options="modelProviderOptions"', '模型配置表单必须使用供应商下拉选项。');
assertIncludes(aiView, ':loading="modelProviderLoading"', '模型配置供应商下拉必须展示加载态。');
assertIncludes(aiView, 'show-search', '模型配置供应商下拉必须支持搜索。');
assertIncludes(aiView, 'placeholder="请选择供应商"', '模型配置供应商下拉必须给出选择提示。');
assertIncludes(aiView, 'loadModelProviderOptions', '进入模型配置时必须加载供应商候选项。');
assertNotIncludes(
  aiView,
  '<a-form-item label="供应商主键" required>',
  '模型配置表单不能直接暴露供应商主键。',
);
assertNotIncludes(
  aiView,
  '<a-input-number v-model:value="modelCommand.providerId" class="full-width" :min="1" />',
  '模型配置表单不能使用数字输入框填写供应商主键。',
);

console.log('AI 模型供应商选择门禁通过。');
