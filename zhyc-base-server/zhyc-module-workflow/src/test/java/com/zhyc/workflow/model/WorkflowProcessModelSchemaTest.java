/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流流程模型表结构测试。
 */
class WorkflowProcessModelSchemaTest {

  /**
   * 验证流程模型表符合首期流程设计管理要求。
   *
   * @throws IOException 读取建表脚本失败时抛出
   */
  @Test
  void shouldDeclareWorkflowProcessModelTableInSchema() throws IOException {
    String sql = Files.readString(Path.of("src/main/resources/db/V1__workflow_core.sql"),
        StandardCharsets.UTF_8).toLowerCase();

    assertTrue(sql.contains("create table if not exists wf_process_model"),
        "should create wf_process_model table");
    assertTrue(sql.contains("tenant_id varchar(64) not null comment '租户业务编码'"),
        "wf_process_model should include tenant_id");
    assertTrue(sql.contains("model_code varchar(128) not null comment '流程模型编码'"),
        "wf_process_model should include model_code");
    assertTrue(sql.contains("model_name varchar(128) not null comment '流程模型名称'"),
        "wf_process_model should include model_name");
    assertTrue(sql.contains("category_id bigint null comment '流程分类 id'"),
        "wf_process_model should include category_id");
    assertTrue(sql.contains("flowable_model_id varchar(128) not null comment 'flowable 模型 id'"),
        "wf_process_model should include flowable_model_id");
    assertTrue(sql.contains("bpmn_xml mediumtext null comment 'bpmn xml 设计稿"),
        "wf_process_model should include bpmn_xml draft");
    assertTrue(sql.contains("unique key uk_wf_model_tenant_code (tenant_id, model_code)"),
        "wf_process_model should include tenant model unique index");
  }
}
