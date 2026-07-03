/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');

const requiredSnippets = [
  ['docs/release/phase1-local-runbook.md', '# ZHYC 快速开发平台首期本地运行手册'],
  ['docs/release/phase1-local-runbook.md', '核心平台'],
  ['docs/release/phase1-local-runbook.md', '认证中心'],
  ['docs/release/phase1-local-runbook.md', '开放 API 网关'],
  ['docs/release/phase1-local-runbook.md', '后台管理端'],
  ['docs/release/phase1-local-runbook.md', 'uni-app 移动端'],
  ['docs/release/phase1-local-runbook.md', 'rtk node scripts/verify-phase1-contracts.mjs --full'],
  ['docs/release/phase1.env.example', 'ZHYC_PLATFORM_DATASOURCE_URL='],
  ['docs/release/phase1.env.example', 'ZHYC_AUTH_CLIENT_SECRET='],
  ['docs/release/phase1.env.example', 'ZHYC_OPENAPI_OAUTH2_INTROSPECTION_URI='],
];

const forbiddenSnippets = [
  ['docs/release/phase1.env.example', 'password123'],
  ['docs/release/phase1.env.example', 'secret123'],
  ['docs/release/phase1.env.example', 'sk-'],
  ['docs/release/phase1.env.example', 'AKIA'],
];

const missingSnippets = requiredSnippets.filter(([file, snippet]) => {
  const path = resolve(workspaceRoot, file);
  return !existsSync(path) || !readFileSync(path, 'utf8').includes(snippet);
});

const forbiddenHits = forbiddenSnippets.filter(([file, snippet]) => {
  const path = resolve(workspaceRoot, file);
  return existsSync(path) && readFileSync(path, 'utf8').includes(snippet);
});

if (missingSnippets.length > 0 || forbiddenHits.length > 0) {
  console.error('首期交付运行材料校验失败。');
  for (const [file, snippet] of missingSnippets) {
    console.error(`缺少关键内容: ${file} -> ${snippet}`);
  }
  for (const [file, snippet] of forbiddenHits) {
    console.error(`存在敏感或弱示例内容: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

console.log('首期交付运行材料校验通过。');
