/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { resolve } from 'node:path';
import { spawnSync } from 'node:child_process';

const serverRoot = process.cwd();
const workspaceRoot = resolve(serverRoot, '..');
const scriptPath = resolve(serverRoot, 'scripts/verify-no-production-placeholders.mjs');

const serverResult = spawnSync('node', [scriptPath], {
  cwd: serverRoot,
  encoding: 'utf8',
});

assert.equal(serverResult.status, 0, serverResult.stderr || serverResult.stdout);
assert.match(serverResult.stdout, /生产源码占位门禁通过/);

const workspaceResult = spawnSync('node', [scriptPath], {
  cwd: workspaceRoot,
  encoding: 'utf8',
});

assert.equal(workspaceResult.status, 0, workspaceResult.stderr || workspaceResult.stdout);
assert.match(workspaceResult.stdout, /生产源码占位门禁通过/);
