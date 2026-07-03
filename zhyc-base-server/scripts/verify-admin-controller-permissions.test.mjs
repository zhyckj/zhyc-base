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

const scriptPath = resolve(process.cwd(), 'scripts/verify-admin-controller-permissions.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-admin-permission-failed-'));
const failedControllerDir = join(failedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/demo/controller');
mkdirSync(failedControllerDir, { recursive: true });
writeFileSync(join(failedControllerDir, 'DemoController.java'), `
package com.zhyc.system.demo.controller;

class DemoController {
  @GetMapping
  Object list() {
    return service.list();
  }
}
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '后台 Controller 方法级映射缺少权限注解时必须失败');
assert.match(failedResult.stderr, /DemoController\.java:5/, '应报告缺少权限注解的映射行');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-admin-permission-passed-'));
const passedControllerDir = join(passedRoot,
  'zhyc-module-system/src/main/java/com/zhyc/system/demo/controller');
mkdirSync(passedControllerDir, { recursive: true });
writeFileSync(join(passedControllerDir, 'DemoController.java'), `
package com.zhyc.system.demo.controller;

class DemoController {
  @RequiresPermissions("system:demo:query")
  @Operation(summary = "分页查询演示数据")
  @GetMapping
  Object list() {
    return service.list();
  }
}
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);

const excludedRoot = mkdtempSync(join(tmpdir(), 'zhyc-admin-permission-excluded-'));
const excludedControllerDir = join(excludedRoot,
  'zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/controller');
mkdirSync(excludedControllerDir, { recursive: true });
writeFileSync(join(excludedControllerDir, 'PurRequestOpenApiController.java'), `
package com.zhyc.purchase.request.controller;

class PurRequestOpenApiController {
  @GetMapping("/{requestNo}/status")
  Object status() {
    return service.status();
  }
}
`);

const excludedResult = spawnSync('node', [scriptPath, excludedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(excludedResult.status, 0, excludedResult.stderr || excludedResult.stdout);

const openApiRoot = mkdtempSync(join(tmpdir(), 'zhyc-admin-permission-openapi-'));
const openApiControllerDir = join(openApiRoot,
  'zhyc-module-purchase/src/main/java/com/zhyc/purchase/order/controller');
mkdirSync(openApiControllerDir, { recursive: true });
writeFileSync(join(openApiControllerDir, 'PurOrderOpenApiController.java'), `
package com.zhyc.purchase.order.controller;

@RequestMapping("/openapi/v1/purchase/orders")
class PurOrderOpenApiController {
  @GetMapping("/{orderNo}")
  Object get() {
    return service.get();
  }
}
`);

const openApiResult = spawnSync('node', [scriptPath, openApiRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(openApiResult.status, 0, openApiResult.stderr || openApiResult.stdout);
