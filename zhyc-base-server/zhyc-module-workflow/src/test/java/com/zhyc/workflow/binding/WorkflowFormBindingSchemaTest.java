/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流表单绑定表结构测试。
 */
class WorkflowFormBindingSchemaTest {

  /**
   * 验证工作流表单绑定表符合首期业务表单接入设计。
   *
   * @throws IOException 读取建表脚本失败时抛出
   */
  @Test
  void shouldDeclareWorkflowFormBindingTableInSchema() throws IOException {
    String sql = Files.readString(Path.of("src/main/resources/db/V1__workflow_core.sql"),
        StandardCharsets.UTF_8).toLowerCase();

    assertTrue(sql.contains("create table if not exists wf_form_binding"),
        "should create wf_form_binding table");
    assertTrue(sql.contains("tenant_id varchar(64) not null comment '租户业务编码'"),
        "wf_form_binding should include tenant_id");
    assertTrue(sql.contains("process_key varchar(128) not null comment '流程定义 key'"),
        "wf_form_binding should include process_key");
    assertTrue(sql.contains("business_module varchar(64) not null comment '业务模块编码'"),
        "wf_form_binding should include business_module");
    assertTrue(sql.contains("business_table varchar(128) not null comment '业务表名'"),
        "wf_form_binding should include business_table");
    assertTrue(sql.contains("form_route varchar(255) not null comment '后台表单路由'"),
        "wf_form_binding should include form_route");
    assertTrue(sql.contains("mobile_route varchar(255) null comment '移动端表单路由'"),
        "wf_form_binding should include mobile_route");
    assertTrue(sql.contains("unique key uk_wf_form_binding_tenant_process (tenant_id, process_key)"),
        "wf_form_binding should include tenant process unique index");
  }
}
