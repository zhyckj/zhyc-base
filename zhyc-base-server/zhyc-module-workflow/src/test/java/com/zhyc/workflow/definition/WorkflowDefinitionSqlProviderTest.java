/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition;

import com.zhyc.workflow.definition.mapper.WorkflowDefinitionSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流流程定义 SQL Provider 测试。
 */
class WorkflowDefinitionSqlProviderTest {

  /**
   * 验证流程定义查询 SQL 包含租户隔离、逻辑删除过滤和稳定排序。
   */
  @Test
  void shouldGenerateListSqlWithTenantIsolation() {
    WorkflowDefinitionSqlProvider provider = new WorkflowDefinitionSqlProvider();

    String sql = provider.selectByTenantId();

    assertTrue(sql.contains("FROM wf_process_definition"));
    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND deleted = 0"));
    assertTrue(sql.contains("ORDER BY process_key, version DESC"));
    assertFalse(sql.contains("SELECT *"));
  }

  /**
   * 验证流程定义保存 SQL 使用租户、流程 key 和版本作为幂等更新键。
   */
  @Test
  void shouldGenerateUpsertSqlWithTenantProcessKeyAndVersion() {
    WorkflowDefinitionSqlProvider provider = new WorkflowDefinitionSqlProvider();

    String sql = provider.upsertDefinition();

    assertTrue(sql.contains("INSERT INTO wf_process_definition"));
    assertTrue(sql.contains("tenant_id"));
    assertTrue(sql.contains("process_key"));
    assertTrue(sql.contains("version"));
    assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    assertTrue(sql.contains("deployment_id = VALUES(deployment_id)"));
  }
}
