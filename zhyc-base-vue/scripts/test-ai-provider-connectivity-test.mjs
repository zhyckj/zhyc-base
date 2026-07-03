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

assertIncludes(aiApi, 'AiProviderTestResponse', 'AI API 必须声明供应商可用性测试响应类型。');
assertIncludes(aiApi, "return request<AiProviderTestResponse, AiProviderSaveRequest>('/ai/providers/test'", 'AI API 必须封装供应商可用性测试接口。');
assertIncludes(aiView, 'testAiProvider', 'AI 供应商页面必须提供测试供应商方法。');
assertIncludes(aiView, ':loading="providerTesting"', '测试供应商按钮必须有加载态。');
assertIncludes(aiView, '@click="testProvider"', '测试供应商按钮必须绑定测试动作。');
assertIncludes(aiView, '测试供应商', '供应商保存区域必须展示测试供应商按钮。');
assertIncludes(aiView, 'providerTestResult', 'AI 供应商页面必须展示最近一次测试结果。');
assertIncludes(aiView, 'provider-test-result', '测试结果必须有独立样式，避免和表单布局混在一起。');

console.log('AI 供应商可用性测试门禁通过。');
