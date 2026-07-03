/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const lowcodeAiFieldUtil = readFileSync(resolve(root, 'src/utils/lowcodeAiField.ts'), 'utf8');
const modelView = readFileSync(resolve(root, 'src/views/lowcode/model/index.vue'), 'utf8');

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

assertIncludes(
  lowcodeAiFieldUtil,
  "LOWCODE_AI_FIELD_APP_CODE = 'lowcode-model-assistant'",
  '低代码建模 AI 字段生成必须使用独立 AI 应用编码，避免复用其他业务提示词。',
);
assertIncludes(
  lowcodeAiFieldUtil,
  "LOWCODE_AI_FIELD_PROMPT_CODE = 'lowcode-field-generate'",
  '低代码建模 AI 字段生成必须使用独立提示词编码。',
);
assertIncludes(
  lowcodeAiFieldUtil,
  'buildLowcodeAiFieldVariables',
  '必须封装 AI runtime 变量构造，避免页面直接拼接散乱提示词。',
);
assertIncludes(
  lowcodeAiFieldUtil,
  'parseLowcodeAiColumns',
  '必须封装 AI JSON 返回解析，保证输出字段可控。',
);
assertIncludes(
  lowcodeAiFieldUtil,
  'mergeLowcodeAiColumns',
  '必须封装字段合并逻辑，保护已有字段不被覆盖。',
);
assertIncludes(
  lowcodeAiFieldUtil,
  'existingCodes',
  'AI 字段合并必须基于已有字段编码去重。',
);
assertIncludes(
  lowcodeAiFieldUtil,
  "REQUIRED_PLATFORM_FIELD_CODES",
  'AI 字段生成必须显式覆盖 id、tenant_id、deleted 等发布必需的平台字段。',
);
assertIncludes(
  modelView,
  'AI 生成字段',
  '数据表建模页必须提供 AI 生成字段入口。',
);
assertIncludes(
  modelView,
  'chatWithAiRuntime',
  '数据表建模页必须接入统一 AI runtime，不得绕过现有审计链路。',
);
assertIncludes(
  modelView,
  'aiGeneratingFields',
  'AI 生成字段按钮必须具备独立加载态。',
);
assertIncludes(
  modelView,
  'handleGenerateColumnsWithAi',
  '数据表建模页必须绑定 AI 字段生成动作。',
);
assertIncludes(
  modelView,
  'ai-generation-alert',
  'AI 字段生成必须保留常驻状态提示，避免错误原因只通过短暂 toast 展示。',
);

console.log('低代码 AI 字段生成门禁通过。');
