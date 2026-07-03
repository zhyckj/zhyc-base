/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { spawnSync } from 'node:child_process';

const scriptPath = resolve(process.cwd(), 'scripts/verify-workflow-runtime-status.mjs');

const failedRoot = mkdtempSync(join(tmpdir(), 'zhyc-workflow-status-fail-'));
writeJava(failedRoot, 'zhyc-module-workflow/src/main/java/com/zhyc/workflow/mapper/DemoSqlProvider.java', `
package com.zhyc.workflow.mapper;

/**
 * 测试 SQL Provider。
 */
public class DemoSqlProvider {

  /**
   * 生成测试 SQL。
   *
   * @return 测试 SQL
   */
  public String selectTodo() {
    return "SELECT * FROM wf_task WHERE status = 'TODO'";
  }
}
`);

const failedResult = spawnSync('node', [scriptPath, failedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(failedResult.status, 0, '工作流运行状态魔法字符串必须触发门禁失败');
assert.match(failedResult.stderr, /DemoSqlProvider\.java/, '应报告包含魔法字符串的 Java 文件');
assert.match(failedResult.stderr, /TODO/, '应报告具体状态编码');

const incompleteEnumRoot = mkdtempSync(join(tmpdir(), 'zhyc-workflow-status-enum-fail-'));
writeJava(incompleteEnumRoot, 'zhyc-module-workflow/src/main/java/com/zhyc/workflow/constant/WorkflowRuntimeStatus.java', `
package com.zhyc.workflow.constant;

/**
 * 工作流运行状态枚举。
 */
public enum WorkflowRuntimeStatus {
  /** 任务待处理。 */
  TODO("TODO");

  /** 状态编码。 */
  private final String code;

  /**
   * 创建状态。
   *
   * @param code 状态编码
   */
  WorkflowRuntimeStatus(String code) {
    this.code = code;
  }
}
`);
writeJava(incompleteEnumRoot, 'zhyc-module-workflow/src/main/java/com/zhyc/workflow/mapper/DemoSqlProvider.java', `
package com.zhyc.workflow.mapper;

import com.zhyc.workflow.constant.WorkflowRuntimeStatus;

/**
 * 测试 SQL Provider。
 */
public class DemoSqlProvider {

  /**
   * 生成测试 SQL。
   *
   * @return 测试 SQL
   */
  public String selectTodo() {
    return "SELECT * FROM wf_task WHERE status = " + WorkflowRuntimeStatus.TODO.name();
  }
}
`);

const incompleteEnumResult = spawnSync('node', [scriptPath, incompleteEnumRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.notEqual(incompleteEnumResult.status, 0, '工作流运行状态枚举缺少中文说明和 fromCode 必须触发门禁失败');
assert.match(incompleteEnumResult.stderr, /getDescription/, '应报告状态中文说明访问方法缺失');
assert.match(incompleteEnumResult.stderr, /fromCode/, '应报告持久化编码解析方法缺失');

const passedRoot = mkdtempSync(join(tmpdir(), 'zhyc-workflow-status-pass-'));
writeJava(passedRoot, 'zhyc-module-workflow/src/main/java/com/zhyc/workflow/constant/WorkflowRuntimeStatus.java', `
package com.zhyc.workflow.constant;

/**
 * 工作流运行状态枚举。
 */
public enum WorkflowRuntimeStatus {
  /** 流程实例运行中。 */
  RUNNING("RUNNING", "流程实例运行中"),
  /** 任务待处理。 */
  TODO("TODO", "任务待处理"),
  /** 任务已审批通过。 */
  APPROVED("APPROVED", "任务已审批通过"),
  /** 任务已驳回。 */
  REJECTED("REJECTED", "任务已驳回"),
  /** 流程或任务已撤回。 */
  REVOKED("REVOKED", "流程或任务已撤回");

  /** 状态编码。 */
  private final String code;

  /** 状态中文说明。 */
  private final String description;

  /**
   * 创建状态。
   *
   * @param code 状态编码
   * @param description 状态中文说明
   */
  WorkflowRuntimeStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 返回状态中文说明。
   *
   * @return 状态中文说明
   */
  public String getDescription() {
    return description;
  }

  /**
   * 根据状态编码解析枚举。
   *
   * @param code 状态编码
   * @return 工作流运行状态
   */
  public static WorkflowRuntimeStatus fromCode(String code) {
    for (WorkflowRuntimeStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("不支持的工作流运行状态编码: " + code);
  }
}
`);
writeJava(passedRoot, 'zhyc-module-workflow/src/main/java/com/zhyc/workflow/mapper/DemoSqlProvider.java', `
package com.zhyc.workflow.mapper;

import com.zhyc.workflow.constant.WorkflowRuntimeStatus;

/**
 * 测试 SQL Provider。
 */
public class DemoSqlProvider {

  /**
   * 生成测试 SQL。
   *
   * @return 测试 SQL
   */
  public String selectTodo() {
    return "SELECT * FROM wf_task WHERE status = " + WorkflowRuntimeStatus.TODO.name();
  }
}
`);

const passedResult = spawnSync('node', [scriptPath, passedRoot], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(passedResult.status, 0, passedResult.stderr || passedResult.stdout);
assert.match(passedResult.stdout, /工作流运行状态门禁通过/);

/**
 * 写入测试用 Java 源码。
 *
 * @param root 测试工程根目录
 * @param file Java 源码相对路径
 * @param content Java 源码内容
 */
function writeJava(root, file, content) {
  const absolutePath = join(root, file);
  mkdirSync(dirname(absolutePath), { recursive: true });
  writeFileSync(absolutePath, content.trim());
}
