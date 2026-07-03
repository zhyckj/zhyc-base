/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { mkdtempSync, mkdirSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { resolve } from 'node:path';

const serverRoot = process.cwd();
const scriptPath = resolve(serverRoot, 'scripts/verify-frontend-route-permissions.mjs');

const missingFixtureRoot = createFixture({
  routesSource: `
    export const adminRoutes = [
      { path: '/system/users', meta: { title: '用户管理', permission: 'system:user:query' } },
      { path: '/purchase/orders', meta: { title: '采购订单', permission: 'purchase:order:query' } },
    ];
  `,
  moduleYamlSources: {
    system: `
moduleCode: system
permissions:
  - system:user:query
`,
    purchase: `
moduleCode: purchase
permissions:
  - purchase:request:view
`,
  },
});

const missingResult = runVerifier(missingFixtureRoot);
assert.equal(missingResult.status, 1, '路由权限未在模块 YAML 声明时必须失败');
assert.match(missingResult.stderr, /purchase:order:query/,
  '失败信息必须指出缺失的具体权限编码');

const validFixtureRoot = createFixture({
  routesSource: `
    export const adminRoutes = [
      { path: '/system/users', meta: { title: '用户管理', permission: 'system:user:query' } },
      { path: '/purchase/orders', meta: { title: '采购订单', permission: 'purchase:order:query' } },
    ];
  `,
  moduleYamlSources: {
    system: `
moduleCode: system
permissions:
  - system:user:query
`,
    purchase: `
moduleCode: purchase
permissions:
  - purchase:order:query
`,
  },
});

const validResult = runVerifier(validFixtureRoot);
assert.equal(validResult.status, 0, validResult.stderr);
assert.match(validResult.stdout, /前端路由权限声明校验通过/);

/**
 * 创建前后端权限声明临时工程。
 *
 * <p>测试只保留路由权限和模块权限声明，便于准确验证门禁行为。</p>
 *
 * @param options 临时工程内容
 * @returns 临时工程根目录
 */
function createFixture(options) {
  const root = mkdtempSync(resolve(tmpdir(), 'zhyc-route-permission-'));
  const routeDir = resolve(root, 'zhyc-base-vue/src/router');
  mkdirSync(routeDir, { recursive: true });
  writeFileSync(resolve(routeDir, 'routes.ts'), options.routesSource);

  for (const [moduleCode, yamlSource] of Object.entries(options.moduleYamlSources)) {
    const moduleDir = resolve(root, `zhyc-base-server/zhyc-module-${moduleCode}/src/main/resources/META-INF`);
    mkdirSync(moduleDir, { recursive: true });
    writeFileSync(resolve(moduleDir, 'zhyc-module.yml'), yamlSource);
  }
  return root;
}

/**
 * 执行前端路由权限声明校验脚本。
 *
 * @param fixtureRoot 临时工程根目录
 * @returns Node 子进程执行结果
 */
function runVerifier(fixtureRoot) {
  return spawnSync(process.execPath, [scriptPath], {
    cwd: serverRoot,
    env: {
      ...process.env,
      ZHYC_FRONTEND_PERMISSION_FIXTURE_ROOT: fixtureRoot,
    },
    encoding: 'utf8',
  });
}
