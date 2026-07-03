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

const violations = [];

for (const file of vueFiles) {
  const source = readFileSync(resolve(rootDir, file), 'utf8');
  if (/:pagination\s*=\s*["']false["']/.test(source)) {
    violations.push(file);
  }
}

if (violations.length > 0) {
  console.error('存在关闭分页的表格：');
  violations.forEach((file) => console.error(`- ${file}`));
  process.exit(1);
}

console.log('表格分页开启校验通过。');
