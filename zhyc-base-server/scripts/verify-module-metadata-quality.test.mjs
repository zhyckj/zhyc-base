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
const scriptPath = resolve(serverRoot, 'scripts/verify-module-metadata-quality.mjs');

const duplicateFixtureRoot = createFixture({
  system: `
moduleCode: system
permissions:
  - system:user:query
menus:
  - system:user
`,
  demo: `
moduleCode: demo
permissions:
  - system:user:query
menus:
  - demo:home
`,
});

const duplicateResult = runVerifier(duplicateFixtureRoot);
assert.equal(duplicateResult.status, 1, '权限跨模块重复登记时必须失败');
assert.match(duplicateResult.stderr, /system:user:query/, '失败信息必须指出重复权限编码');

const validFixtureRoot = createFixture({
  system: `
moduleCode: system
permissions:
  - system:user:query
menus:
  - system:user
`,
  demo: `
moduleCode: demo
permissions:
  - demo:home:query
menus:
  - demo:home
`,
});

const validResult = runVerifier(validFixtureRoot);
assert.equal(validResult.status, 0, validResult.stderr);
assert.match(validResult.stdout, /模块元数据质量校验通过/);

/**
 * 创建模块元数据临时工程。
 *
 * @param modules 模块编码到 YAML 内容的映射
 * @returns 临时后端工程根目录
 */
function createFixture(modules) {
  const root = mkdtempSync(resolve(tmpdir(), 'zhyc-module-quality-'));
  for (const [folderName, yamlSource] of Object.entries(modules)) {
    const moduleDir = resolve(root, `zhyc-module-${folderName}/src/main/resources/META-INF`);
    mkdirSync(moduleDir, { recursive: true });
    writeFileSync(resolve(moduleDir, 'zhyc-module.yml'), yamlSource);
  }
  return root;
}

/**
 * 执行模块元数据质量校验脚本。
 *
 * @param fixtureRoot 临时后端工程根目录
 * @returns Node 子进程执行结果
 */
function runVerifier(fixtureRoot) {
  return spawnSync(process.execPath, [scriptPath], {
    cwd: serverRoot,
    env: {
      ...process.env,
      ZHYC_MODULE_METADATA_FIXTURE_ROOT: fixtureRoot,
    },
    encoding: 'utf8',
  });
}
