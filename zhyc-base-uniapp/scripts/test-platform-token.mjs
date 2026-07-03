/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { Buffer } from 'node:buffer';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import ts from 'typescript';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));
const sourcePath = resolve(rootDir, 'src/utils/platformToken.ts');

assert.ok(existsSync(sourcePath), '移动端平台令牌解析工具文件必须存在');

const sourceCode = readFileSync(sourcePath, 'utf8');
const transpiled = ts.transpileModule(sourceCode, {
  compilerOptions: {
    module: ts.ModuleKind.ES2022,
    target: ts.ScriptTarget.ES2022,
  },
}).outputText;
const moduleUrl = `data:text/javascript;base64,${Buffer.from(transpiled).toString('base64')}`;
const { parsePlatformTokenClaims } = await import(moduleUrl);

assert.equal(typeof parsePlatformTokenClaims, 'function', '必须导出 parsePlatformTokenClaims 函数');

function buildToken(payload) {
  const header = Buffer.from(JSON.stringify({ alg: 'RS256', typ: 'JWT' })).toString('base64url');
  const body = Buffer.from(JSON.stringify(payload)).toString('base64url');
  return `${header}.${body}.signature`;
}

const claims = parsePlatformTokenClaims(
  buildToken({
    tenant_id: 'tenant_mobile',
    user_id: '2002',
    preferred_username: 'mobile_user',
  }),
);

assert.deepEqual(claims, {
  tenantId: 'tenant_mobile',
  userId: 2002,
  accountName: 'mobile_user',
});
assert.equal(parsePlatformTokenClaims(''), null, '空令牌必须返回 null');
assert.equal(parsePlatformTokenClaims(buildToken({ tenant_id: '', user_id: 2002, preferred_username: 'mobile_user' })), null);
assert.equal(parsePlatformTokenClaims(buildToken({ tenant_id: 'tenant_mobile', user_id: -1, preferred_username: 'mobile_user' })), null);

console.log('移动端平台令牌 Claims 解析测试通过。');
