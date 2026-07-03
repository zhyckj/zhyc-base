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

const scriptPath = resolve(process.cwd(), 'scripts/verify-sql-safety.mjs');
const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-sql-safety-'));
const failedDbDir = join(failedRoot, 'zhyc-module-demo/src/main/resources/db');
mkdirSync(failedDbDir, { recursive: true });
writeFileSync(join(failedDbDir, 'V1__unsafe.sql'), `
SELECT * FROM demo_order;
UPDATE demo_order SET status = 'closed';
DELETE FROM demo_order;
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '生产 SQL 存在 SELECT * 或无条件变更时必须失败');
assert.match(failedResult.stderr, /V1__unsafe\.sql:2/, '应报告 SELECT * 位置');
assert.match(failedResult.stderr, /V1__unsafe\.sql:3/, '应报告无条件 UPDATE 位置');
assert.match(failedResult.stderr, /V1__unsafe\.sql:4/, '应报告无条件 DELETE 位置');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-sql-safety-pass-'));
const passedDbDir = join(passedRoot, 'zhyc-module-demo/src/main/resources/db');
mkdirSync(passedDbDir, { recursive: true });
writeFileSync(join(passedDbDir, 'V1__safe.sql'), `
SELECT id, tenant_id, order_no FROM demo_order WHERE tenant_id = 'tenant_a';
UPDATE demo_order SET status = 'closed' WHERE tenant_id = 'tenant_a' AND order_no = 'PO001';
DELETE FROM demo_order WHERE tenant_id = 'tenant_a' AND order_no = 'PO001';
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /SQL 安全门禁通过/);
