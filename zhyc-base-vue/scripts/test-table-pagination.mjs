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
const tablePaginationSource = readFileSync(resolve(rootDir, 'src/utils/tablePagination.ts'), 'utf8');
const paginationFalseAllowList = new Set([
  'src/views/ai/core/index.vue',
  'src/views/lowcode/page/index.vue',
  'src/views/system/security-protection/index.vue',
]);
const vueFiles = execFileSync('rg', ['--files', 'src/views'], {
  cwd: rootDir,
  encoding: 'utf8',
}).trim().split('\n').filter(Boolean);

const violations = [];

if (!/defaultPageSize\s*:\s*10/.test(tablePaginationSource)) {
  violations.push('src/utils/tablePagination.ts 缺少 defaultPageSize: 10，普通表格无法获得统一默认页大小。');
}

if (/\bpageSize\s*:/.test(tablePaginationSource)) {
  violations.push('src/utils/tablePagination.ts 不能在全局分页对象中固定 pageSize，否则切换 20 条/页后会被重新渲染覆盖。');
}

for (const file of vueFiles) {
  const source = readFileSync(resolve(rootDir, file), 'utf8');
  if (!paginationFalseAllowList.has(file) && /:pagination\s*=\s*["']false["']/.test(source)) {
    violations.push(`${file} 存在关闭分页的表格。`);
  }
}

if (violations.length > 0) {
  console.error('表格分页配置校验失败：');
  violations.forEach((message) => console.error(`- ${message}`));
  process.exit(1);
}

console.log('表格分页开启校验通过。');
