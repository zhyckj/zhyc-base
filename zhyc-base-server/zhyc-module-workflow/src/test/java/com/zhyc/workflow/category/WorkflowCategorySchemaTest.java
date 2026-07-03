/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流分类表结构测试。
 */
class WorkflowCategorySchemaTest {

  /**
   * 验证工作流分类表符合首期流程分类设计和租户隔离要求。
   *
   * @throws IOException 读取建表脚本失败时抛出
   */
  @Test
  void shouldDeclareWorkflowCategoryTableInSchema() throws IOException {
    String sql = Files.readString(Path.of("src/main/resources/db/V1__workflow_core.sql"),
        StandardCharsets.UTF_8).toLowerCase();

    assertTrue(sql.contains("create table if not exists wf_category"),
        "should create wf_category table");
    assertTrue(sql.contains("tenant_id varchar(64) not null comment '租户业务编码'"),
        "wf_category should include tenant_id");
    assertTrue(sql.contains("category_code varchar(64) not null comment '流程分类编码'"),
        "wf_category should include category_code");
    assertTrue(sql.contains("category_name varchar(128) not null comment '流程分类名称'"),
        "wf_category should include category_name");
    assertTrue(sql.contains("sort_order int not null default 0 comment '排序号'"),
        "wf_category should include sort_order");
    assertTrue(sql.contains("unique key uk_wf_category_tenant_code (tenant_id, category_code)"),
        "wf_category should include tenant category unique index");
  }
}
