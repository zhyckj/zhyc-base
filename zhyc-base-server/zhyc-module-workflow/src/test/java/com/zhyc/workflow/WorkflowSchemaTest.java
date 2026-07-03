/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * 工作流核心表结构测试。
 */
class WorkflowSchemaTest {

  /**
   * 验证首期工作流表包含流程实例、待办任务和审批记录三类核心数据。
   *
   * @throws IOException 读取建表脚本失败时抛出
   */
  @Test
  void shouldDefineWorkflowCoreTables() throws IOException {
    String sql = Files.readString(Path.of("src/main/resources/db/V1__workflow_core.sql"),
        StandardCharsets.UTF_8);

    assertTrue(sql.contains("wf_process_instance"));
    assertTrue(sql.contains("wf_task"));
    assertTrue(sql.contains("wf_approval_record"));
    assertTrue(sql.contains("tenant_id"));
    assertTrue(sql.contains("process_instance_id"));
    assertTrue(sql.contains("assignee_user_id"));
  }
}
