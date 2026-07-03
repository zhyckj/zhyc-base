/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import { spawnSync } from 'node:child_process';

const scriptPath = resolve(process.cwd(), 'scripts/verify-no-hardcoded-context.mjs');
const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-hardcoded-context-'));
const failedVueDir = join(failedRoot, 'zhyc-base-vue/src/views/demo');
mkdirSync(failedVueDir, { recursive: true });
writeFileSync(join(failedVueDir, 'index.ts'), `
export const tenantId = 'tenant_a';
export const currentUserId = 1001;
export const command = { orgId: 1 };
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '生产源码硬编码租户或用户上下文必须触发门禁失败');
assert.match(failedResult.stderr, /index\.ts:2/, '应报告硬编码租户位置');
assert.match(failedResult.stderr, /index\.ts:3/, '应报告硬编码用户位置');
assert.match(failedResult.stderr, /index\.ts:4/, '应报告硬编码组织位置');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-hardcoded-context-pass-'));
const passedVueDir = join(passedRoot, 'zhyc-base-vue/src/views/demo');
mkdirSync(passedVueDir, { recursive: true });
writeFileSync(join(passedVueDir, 'index.ts'), `
import { requireAdminOrgId, requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

export const tenantId = requireAdminTenantId();
export const currentUserId = requireAdminUserId();
export const command = { orgId: requireAdminOrgId() };
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /硬编码上下文门禁通过/);
