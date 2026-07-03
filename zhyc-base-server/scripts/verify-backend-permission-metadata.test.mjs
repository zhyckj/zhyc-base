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
const scriptPath = resolve(serverRoot, 'scripts/verify-backend-permission-metadata.mjs');

const missingFixtureRoot = createFixture({
  controllerSource: `
package com.zhyc.system.demo;

import org.apache.shiro.authz.annotation.RequiresPermissions;

class DemoController {
  @RequiresPermissions("system:demo:save")
  void save() {
  }
}
`,
  yamlSource: `
moduleCode: system
permissions:
  - system:demo:query
`,
});

const missingResult = runVerifier(missingFixtureRoot);
assert.equal(missingResult.status, 1, '后端注解权限未在模块 YAML 声明时必须失败');
assert.match(missingResult.stderr, /system:demo:save/, '失败信息必须指出缺失的权限编码');

const validFixtureRoot = createFixture({
  controllerSource: `
package com.zhyc.system.demo;

import org.apache.shiro.authz.annotation.RequiresPermissions;

class DemoController {
  @RequiresPermissions("system:demo:save")
  void save() {
  }
}
`,
  yamlSource: `
moduleCode: system
permissions:
  - system:demo:save
`,
});

const validResult = runVerifier(validFixtureRoot);
assert.equal(validResult.status, 0, validResult.stderr);
assert.match(validResult.stdout, /后端权限元数据校验通过/);

/**
 * 创建后端权限注解和模块元数据临时工程。
 *
 * @param options 临时工程内容
 * @returns 临时工程根目录
 */
function createFixture(options) {
  const root = mkdtempSync(resolve(tmpdir(), 'zhyc-backend-permission-'));
  const controllerDir = resolve(root, 'zhyc-module-system/src/main/java/com/zhyc/system/demo');
  mkdirSync(controllerDir, { recursive: true });
  writeFileSync(resolve(controllerDir, 'DemoController.java'), options.controllerSource);

  const moduleDir = resolve(root, 'zhyc-module-system/src/main/resources/META-INF');
  mkdirSync(moduleDir, { recursive: true });
  writeFileSync(resolve(moduleDir, 'zhyc-module.yml'), options.yamlSource);
  return root;
}

/**
 * 执行后端权限元数据校验脚本。
 *
 * @param fixtureRoot 临时后端工程根目录
 * @returns Node 子进程执行结果
 */
function runVerifier(fixtureRoot) {
  return spawnSync(process.execPath, [scriptPath], {
    cwd: serverRoot,
    env: {
      ...process.env,
      ZHYC_BACKEND_PERMISSION_FIXTURE_ROOT: fixtureRoot,
    },
    encoding: 'utf8',
  });
}
