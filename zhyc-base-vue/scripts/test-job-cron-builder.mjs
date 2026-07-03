/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import assert from 'node:assert/strict';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));
const jobTaskVue = readFileSync(resolve(rootDir, 'src/views/job/task/index.vue'), 'utf8');

assert.match(jobTaskVue, /cronBuilderMode/, '在线作业表单应提供 Cron 生成模式选择。');
assert.match(jobTaskVue, /buildCronExpression/, '在线作业表单应通过选择项生成 Cron 表达式。');
assert.match(jobTaskVue, /cronPreviewText/, '在线作业表单应展示根据选择生成的 Cron 表达式。');
assert.match(jobTaskVue, /syncCronExpressionFromBuilder/, '在线作业表单应在选择项变化后同步 Cron 表达式。');
assert.doesNotMatch(
  jobTaskVue,
  /<a-input\s+v-model:value="formState\.cronExpression"/,
  'Cron 表达式不应再作为主要手输输入框。',
);
