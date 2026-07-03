/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流流程定义表结构测试。
 */
class WorkflowDefinitionSchemaTest {

  /**
   * 验证流程定义表符合首期流程部署版本管理设计。
   *
   * @throws IOException 读取建表脚本失败时抛出
   */
  @Test
  void shouldDeclareWorkflowDefinitionTableInSchema() throws IOException {
    String sql = Files.readString(Path.of("src/main/resources/db/V1__workflow_core.sql"),
        StandardCharsets.UTF_8).toLowerCase();

    assertTrue(sql.contains("create table if not exists wf_process_definition"),
        "should create wf_process_definition table");
    assertTrue(sql.contains("tenant_id varchar(64) not null comment '租户业务编码'"),
        "wf_process_definition should include tenant_id");
    assertTrue(sql.contains("process_key varchar(128) not null comment '流程定义 key'"),
        "wf_process_definition should include process_key");
    assertTrue(sql.contains("process_name varchar(128) not null comment '流程定义名称'"),
        "wf_process_definition should include process_name");
    assertTrue(sql.contains("version int not null comment '流程定义版本号'"),
        "wf_process_definition should include version");
    assertTrue(sql.contains("deployment_id varchar(128) not null comment 'flowable 部署 id'"),
        "wf_process_definition should include deployment_id");
    assertTrue(sql.contains("unique key uk_wf_definition_tenant_key_version (tenant_id, process_key, version)"),
        "wf_process_definition should include tenant process version unique index");
  }
}
