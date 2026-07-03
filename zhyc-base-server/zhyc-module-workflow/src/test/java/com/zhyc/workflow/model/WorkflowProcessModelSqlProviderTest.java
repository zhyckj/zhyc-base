/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model;

import com.zhyc.workflow.model.mapper.WorkflowProcessModelSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流流程模型 SQL Provider 测试。
 */
class WorkflowProcessModelSqlProviderTest {

  /**
   * 验证流程模型查询 SQL 包含租户隔离、逻辑删除过滤和稳定排序。
   */
  @Test
  void shouldGenerateListSqlWithTenantIsolation() {
    WorkflowProcessModelSqlProvider provider = new WorkflowProcessModelSqlProvider();

    String sql = provider.selectByTenantId();

    assertTrue(sql.contains("FROM wf_process_model"));
    assertTrue(sql.contains("bpmn_xml AS bpmnXml"));
    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND deleted = 0"));
    assertTrue(sql.contains("ORDER BY category_id, model_code"));
    assertFalse(sql.contains("SELECT *"));
  }

  /**
   * 验证流程模型保存 SQL 使用租户和模型编码作为幂等更新键。
   */
  @Test
  void shouldGenerateUpsertSqlWithTenantAndModelCode() {
    WorkflowProcessModelSqlProvider provider = new WorkflowProcessModelSqlProvider();

    String sql = provider.upsertModel();

    assertTrue(sql.contains("INSERT INTO wf_process_model"));
    assertTrue(sql.contains("tenant_id"));
    assertTrue(sql.contains("model_code"));
    assertTrue(sql.contains("flowable_model_id"));
    assertTrue(sql.contains("bpmn_xml"));
    assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    assertTrue(sql.contains("model_name = VALUES(model_name)"));
    assertTrue(sql.contains("bpmn_xml = VALUES(bpmn_xml)"));
  }
}
