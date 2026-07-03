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

const scriptPath = resolve(process.cwd(), 'scripts/verify-request-body-null-guard.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-request-body-guard-failed-'));
const failedControllerDir = join(failedRoot,
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/demo/controller');
mkdirSync(failedControllerDir, { recursive: true });
writeFileSync(join(failedControllerDir, 'DemoController.java'), `
package com.zhyc.system.demo.controller;

class DemoController {
  void save(DemoRequest request) {
    DemoRequest safeRequest = request == null ? new DemoRequest() : request;
    service.save(safeRequest);
  }
}
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '非工作流控制器空请求体兜底必须触发门禁失败');
assert.match(failedResult.stderr, /DemoController\.java:6/, '应报告空对象兜底位置');

const workflowAllowedRoot = mkdtempSync(join(tmpdir(), 'zhyc-request-body-guard-workflow-'));
const workflowControllerDir = join(workflowAllowedRoot,
  'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow/demo/controller');
mkdirSync(workflowControllerDir, { recursive: true });
writeFileSync(join(workflowControllerDir, 'WorkflowDemoController.java'), `
package com.zhyc.workflow.demo.controller;

class WorkflowDemoController {
  void save(DemoRequest request) {
    DemoRequest safeRequest = request == null ? new DemoRequest() : request;
    service.save(safeRequest);
  }
}
`);

const workflowAllowedResult = spawnSync('node', [scriptPath, workflowAllowedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(workflowAllowedResult.status, 0,
  workflowAllowedResult.stderr || workflowAllowedResult.stdout);

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-request-body-guard-passed-'));
const passedControllerDir = join(passedRoot,
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/demo/controller');
mkdirSync(passedControllerDir, { recursive: true });
writeFileSync(join(passedControllerDir, 'DemoController.java'), `
package com.zhyc.system.demo.controller;

class DemoController {
  void save(DemoRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("演示请求不能为空");
    }
    service.save(request);
  }
}
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /RequestBody 空请求体门禁通过/);
