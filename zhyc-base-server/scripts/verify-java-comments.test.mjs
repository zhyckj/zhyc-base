/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { mkdtempSync, mkdirSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import { spawnSync } from 'node:child_process';

const scriptPath = resolve(process.cwd(), 'scripts/verify-java-comments.mjs');
const fixtureRoot = mkdtempSync(join(tmpdir(), 'zhyc-java-comments-'));
const sourceRoot = join(fixtureRoot, 'src/main/java/com/zhyc/demo');
mkdirSync(sourceRoot, { recursive: true });

writeFileSync(join(sourceRoot, 'MissingCommentDemo.java'), `
package com.zhyc.demo;

public class MissingCommentDemo {
  private final String tenantId;

  public String getTenantId() {
    return tenantId;
  }
}
`);

writeFileSync(join(sourceRoot, 'CommentedDemo.java'), `
package com.zhyc.demo;

/**
 * 注释完整的演示类。
 *
 * <p>用于验证 Java 中文注释门禁可以识别合规声明。</p>
 */
public class CommentedDemo {
  /** 租户业务编码，用于验证字段中文注释。 */
  private final String tenantId;

  /**
   * 创建注释完整的演示对象。
   *
   * @param tenantId 租户业务编码
   */
  public CommentedDemo(String tenantId) {
    this.tenantId = tenantId;
  }

  /**
   * 获取租户业务编码。
   *
   * @return 租户业务编码
   */
  public String getTenantId() {
    return tenantId;
  }
}
`);

const failedResult = spawnSync('node', [scriptPath, sourceRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '缺少中文注释的 Java 声明必须触发门禁失败');
assert.match(failedResult.stderr, /MissingCommentDemo\.java:4/, '应报告缺少类注释的位置');
assert.match(failedResult.stderr, /MissingCommentDemo\.java:5/, '应报告缺少字段注释的位置');
assert.match(failedResult.stderr, /MissingCommentDemo\.java:7/, '应报告缺少方法注释的位置');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-java-comments-pass-'));
const passedSourceRoot = join(passedRoot, 'src/main/java/com/zhyc/demo');
mkdirSync(passedSourceRoot, { recursive: true });
writeFileSync(join(passedSourceRoot, 'CommentedDemo.java'), `
package com.zhyc.demo;

/**
 * 注释完整的演示类。
 *
 * <p>用于验证 Java 中文注释门禁可以识别合规声明。</p>
 */
public class CommentedDemo {
  /** 租户业务编码，用于验证字段中文注释。 */
  private final String tenantId;

  /**
   * 创建注释完整的演示对象。
   *
   * @param tenantId 租户业务编码
   */
  public CommentedDemo(String tenantId) {
    this.tenantId = tenantId;
  }

  /**
   * 获取租户业务编码。
   *
   * @return 租户业务编码
   */
  public String getTenantId() {
    return tenantId;
  }
}
`);

writeFileSync(join(passedSourceRoot, 'AccessorDemo.java'), `
package com.zhyc.demo;

/**
 * 访问器注释演示类。
 *
 * <p>用于验证简单 JavaBean getter 可以复用字段中文注释承载业务语义。</p>
 */
public class AccessorDemo {
  /** 租户业务编码，用于验证访问器继承字段注释语义。 */
  private final String tenantId;

  /**
   * 创建访问器注释演示对象。
   *
   * @param tenantId 租户业务编码
   */
  public AccessorDemo(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getTenantId() {
    return tenantId;
  }
}
`);

const passedResult = spawnSync('node', [scriptPath, passedSourceRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /Java 中文注释门禁通过/);
