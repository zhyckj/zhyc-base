/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.workflow.constant.WorkflowRuntimeStatus;
import org.junit.jupiter.api.Test;

/**
 * 工作流运行状态测试。
 */
class WorkflowRuntimeStatusTest {

  /**
   * 验证工作流运行状态编码稳定，避免任务状态和流程实例状态散落为魔法字符串。
   */
  @Test
  void shouldExposeStableRuntimeStatusCodes() {
    assertEquals("RUNNING", WorkflowRuntimeStatus.RUNNING.getCode());
    assertEquals("TODO", WorkflowRuntimeStatus.TODO.getCode());
    assertEquals("APPROVED", WorkflowRuntimeStatus.APPROVED.getCode());
    assertEquals("REJECTED", WorkflowRuntimeStatus.REJECTED.getCode());
    assertEquals("REVOKED", WorkflowRuntimeStatus.REVOKED.getCode());
  }

  /**
   * 验证 SQL Provider 可复用状态枚举生成安全 SQL 字面量。
   */
  @Test
  void shouldExposeSqlLiteralForProvider() {
    assertEquals("'TODO'", WorkflowRuntimeStatus.TODO.sqlLiteral());
    assertEquals("'RUNNING'", WorkflowRuntimeStatus.RUNNING.sqlLiteral());
  }

  /**
   * 验证工作流运行状态携带中文说明，便于后台、移动端和审计记录展示。
   */
  @Test
  void shouldExposeRuntimeStatusDescriptions() {
    assertEquals("流程实例运行中", WorkflowRuntimeStatus.RUNNING.getDescription());
    assertEquals("任务待处理", WorkflowRuntimeStatus.TODO.getDescription());
    assertEquals("任务已审批通过", WorkflowRuntimeStatus.APPROVED.getDescription());
    assertEquals("任务已驳回", WorkflowRuntimeStatus.REJECTED.getDescription());
    assertEquals("流程或任务已撤回", WorkflowRuntimeStatus.REVOKED.getDescription());
  }

  /**
   * 验证工作流运行状态可从持久化编码恢复，未知编码必须被明确拒绝。
   */
  @Test
  void shouldParseRuntimeStatusFromCode() {
    assertEquals(WorkflowRuntimeStatus.TODO, WorkflowRuntimeStatus.fromCode("TODO"));

    assertThrows(IllegalArgumentException.class, () -> WorkflowRuntimeStatus.fromCode("PENDING"));
  }
}
