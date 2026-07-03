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

const scriptPath = resolve(process.cwd(), 'scripts/verify-module-boundaries.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-module-boundary-fail-'));
writePom(failedRoot, 'zhyc-module-purchase/pom.xml', `
<project>
  <artifactId>zhyc-module-purchase</artifactId>
  <dependencies>
    <dependency>
      <groupId>com.zhyc</groupId>
      <artifactId>zhyc-module-workflow</artifactId>
      <version>\${project.version}</version>
    </dependency>
  </dependencies>
</project>
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '业务模块直接依赖其他业务模块必须触发门禁失败');
assert.match(failedResult.stderr, /zhyc-module-purchase/, '应报告发起依赖的业务模块');
assert.match(failedResult.stderr, /zhyc-module-workflow/, '应报告被直接依赖的业务模块');

const failedImportRoot = mkdtempSync(join(tmpdir(), 'zhyc-module-boundary-import-fail-'));
writePom(failedImportRoot, 'zhyc-module-purchase/pom.xml', `
<project>
  <artifactId>zhyc-module-purchase</artifactId>
</project>
`);
writeJava(failedImportRoot,
    'zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/DemoService.java',
    `
package com.zhyc.purchase.request;

import com.zhyc.workflow.service.WorkflowTaskService;

class DemoService {
  private WorkflowTaskService workflowTaskService;
}
`);

const failedImportResult = spawnSync('node', [scriptPath, failedImportRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedImportResult.status, 0, '业务模块源码 import 其他业务模块必须触发门禁失败');
assert.match(failedImportResult.stderr, /DemoService\.java/, '应报告违规源码文件');
assert.match(failedImportResult.stderr, /com\.zhyc\.workflow/, '应报告违规 import 包');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-module-boundary-pass-'));
writePom(passedRoot, 'zhyc-module-purchase/pom.xml', `
<project>
  <artifactId>zhyc-module-purchase</artifactId>
  <dependencies>
    <dependency>
      <groupId>com.zhyc</groupId>
      <artifactId>zhyc-common</artifactId>
      <version>\${project.version}</version>
    </dependency>
  </dependencies>
</project>
`);
writePom(passedRoot, 'zhyc-platform-app/pom.xml', `
<project>
  <artifactId>zhyc-platform-app</artifactId>
  <dependencies>
    <dependency>
      <groupId>com.zhyc</groupId>
      <artifactId>zhyc-module-workflow</artifactId>
      <version>\${project.version}</version>
    </dependency>
  </dependencies>
</project>
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /模块边界门禁通过/);

/**
 * 写入测试 POM。
 *
 * @param root 测试工作区根目录
 * @param file POM 相对路径
 * @param content POM 内容
 */
function writePom(root, file, content) {
  const absolutePath = join(root, file);
  mkdirSync(resolve(absolutePath, '..'), { recursive: true });
  writeFileSync(absolutePath, content.trim());
}

/**
 * 写入测试 Java 源码。
 *
 * @param root 测试工作区根目录
 * @param file Java 文件相对路径
 * @param content Java 源码内容
 */
function writeJava(root, file, content) {
  const absolutePath = join(root, file);
  mkdirSync(resolve(absolutePath, '..'), { recursive: true });
  writeFileSync(absolutePath, content.trim());
}
