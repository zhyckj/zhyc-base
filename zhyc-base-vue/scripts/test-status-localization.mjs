/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import { execFileSync } from 'node:child_process';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));

const vueFiles = execFileSync('rg', ['--files', 'src/views'], {
  cwd: rootDir,
  encoding: 'utf8',
}).trim().split('\n').filter(Boolean);

const rawStatusPatterns = [
  /\{\{\s*record\.status\s*\}\}/,
  /\{\{\s*\(?\s*record\.status\s*\)?\s*\}\}/,
  /\{\{\s*detail\.status\s*\}\}/,
  /\{\{\s*\(?\s*detail\.status\s*\)?\s*\}\}/,
  /\{\{\s*generationRecord\.status\s*\}\}/,
  /\{\{\s*\(?\s*generationRecord\.status\s*\)?\s*\}\}/,
  /\?\s*['"]启用['"]\s*:\s*record\.status/,
  /\?\s*['"]已发布['"]\s*:\s*record\.status/,
  /<a-select-option[^>]*>\s*(enabled|disabled|published|draft|active|inactive)\s*<\/a-select-option>/,
];

const ignoredFiles = new Set([
  'src/views/workflow/task/started.vue',
  'src/views/workflow/task/todo.vue',
  'src/views/workflow/task/done.vue',
  'src/views/workflow/task/cc.vue',
]);

const violations = [];

for (const file of vueFiles) {
  if (ignoredFiles.has(file)) {
    continue;
  }
  const source = readFileSync(resolve(rootDir, file), 'utf8');
  rawStatusPatterns.forEach((pattern) => {
    if (pattern.test(source)) {
      violations.push(`${file} -> ${pattern}`);
    }
  });
}

if (violations.length > 0) {
  console.error('存在未中文化的状态展示：');
  violations.forEach((violation) => console.error(`- ${violation}`));
  process.exit(1);
}

console.log('状态展示中文化校验通过。');
