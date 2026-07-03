/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const defaultsSource = readFileSync(resolve(root, 'src/utils/aiProviderDefaults.ts'), 'utf8');
const aiView = readFileSync(resolve(root, 'src/views/ai/core/index.vue'), 'utf8');

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

assertIncludes(
  defaultsSource,
  "deepseek: 'https://api.deepseek.com'",
  'DeepSeek 供应商类型必须自动带出官方 OpenAI 兼容基础地址。',
);
assertIncludes(
  defaultsSource,
  "dashscope: 'https://dashscope.aliyuncs.com/compatible-mode/v1'",
  '通义千问供应商类型必须自动带出 DashScope OpenAI 兼容基础地址。',
);
assertIncludes(
  defaultsSource,
  "volcengine: 'https://ark.cn-beijing.volces.com/api/v3'",
  '火山方舟供应商类型必须自动带出 Ark OpenAI 兼容基础地址。',
);
assertIncludes(
  defaultsSource,
  "zhipu: 'https://open.bigmodel.cn/api/paas/v4'",
  '智谱供应商类型必须自动带出 OpenAI 兼容基础地址。',
);
assertIncludes(
  defaultsSource,
  'shouldApplyAiProviderDefaultBaseUrl',
  '必须提供是否允许覆盖基础地址的判断函数，避免覆盖用户手动修改值。',
);
assertIncludes(
  aiView,
  '@change="handleProviderTypeChange"',
  '供应商类型下拉必须绑定变更事件，选择类型后自动补充基础地址。',
);
assertIncludes(
  aiView,
  '@change="markProviderBaseUrlTouched"',
  '基础地址输入框必须记录用户手动修改，后续切换类型不得覆盖手动地址。',
);

console.log('AI 供应商默认基础地址门禁通过。');
