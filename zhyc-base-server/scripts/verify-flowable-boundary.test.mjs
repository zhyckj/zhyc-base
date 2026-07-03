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

const scriptPath = resolve(process.cwd(), 'scripts/verify-flowable-boundary.mjs');
const fixtureRoot = mkdtempSync(join(tmpdir(), 'zhyc-flowable-boundary-'));

const workflowDir = join(fixtureRoot,
    'zhyc-module-workflow/src/main/java/com/zhyc/workflow');
mkdirSync(workflowDir, { recursive: true });
writeFileSync(join(workflowDir, 'AllowedWorkflowAdapter.java'), `
package com.zhyc.workflow;

import org.flowable.engine.RuntimeService;

/**
 * 允许访问 Flowable 的工作流适配器。
 */
public class AllowedWorkflowAdapter {
  /** Flowable 运行时服务。 */
  private RuntimeService runtimeService;
}
`);

const businessDir = join(fixtureRoot,
    'zhyc-module-purchase/src/main/java/com/zhyc/purchase');
mkdirSync(businessDir, { recursive: true });
writeFileSync(join(businessDir, 'IllegalPurchaseService.java'), `
package com.zhyc.purchase;

import org.flowable.engine.TaskService;

/**
 * 违规直接访问 Flowable 的业务服务。
 */
public class IllegalPurchaseService {
  /** Flowable 任务服务。 */
  private TaskService taskService;
}
`);

const failedResult = spawnSync('node', [scriptPath, fixtureRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '非工作流模块直接导入 Flowable 必须触发门禁失败');
assert.match(failedResult.stderr, /IllegalPurchaseService\.java:4/,
    '应报告违规业务模块 Flowable 导入位置');
assert.doesNotMatch(failedResult.stderr, /AllowedWorkflowAdapter/,
    '工作流模块内部 Flowable 适配器不应被误报');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-flowable-boundary-pass-'));
const passedBusinessDir = join(passedRoot,
    'zhyc-module-purchase/src/main/java/com/zhyc/purchase');
mkdirSync(passedBusinessDir, { recursive: true });
writeFileSync(join(passedBusinessDir, 'PurchaseWorkflowFacadeCaller.java'), `
package com.zhyc.purchase;

import com.zhyc.common.workflow.WorkflowService;

/**
 * 通过平台工作流门面发起审批的业务服务。
 */
public class PurchaseWorkflowFacadeCaller {
  /** 平台工作流门面。 */
  private WorkflowService workflowService;
}
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /Flowable 边界门禁通过/);
