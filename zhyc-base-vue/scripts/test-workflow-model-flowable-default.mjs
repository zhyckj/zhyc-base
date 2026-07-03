/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));
const source = readFileSync(resolve(rootDir, 'src/views/workflow/model/index.vue'), 'utf8');

const checks = [
  {
    passed: /label="Flowable 模型 ID（可选）"/.test(source),
    message: '流程模型表单应把 Flowable 字段标识为可选模型 ID。',
  },
  {
    passed: /<a-input\s+v-model:value="formState\.flowableModelId"/.test(source),
    message: '流程模型表单不应使用空下拉选择 Flowable 模型。',
  },
  {
    passed: /watch\(\s*\(\)\s*=>\s*formState\.modelCode/.test(source)
      && /formState\.flowableModelId\s*=\s*nextModelCode\.trim\(\)/.test(source),
    message: '新增流程模型时应自动使用模型编码填充 Flowable 模型 ID。',
  },
];

const failures = checks.filter((check) => !check.passed);

if (failures.length > 0) {
  console.error('流程模型 Flowable ID 默认值校验失败：');
  failures.forEach((failure) => console.error(`- ${failure.message}`));
  process.exit(1);
}

console.log('流程模型 Flowable ID 默认值校验通过。');
