/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding;

import com.zhyc.workflow.binding.mapper.WorkflowFormBindingSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流表单绑定 SQL Provider 测试。
 */
class WorkflowFormBindingSqlProviderTest {

  /**
   * 验证表单绑定查询 SQL 包含租户隔离、逻辑删除过滤和稳定排序。
   */
  @Test
  void shouldGenerateListSqlWithTenantIsolation() {
    WorkflowFormBindingSqlProvider provider = new WorkflowFormBindingSqlProvider();

    String sql = provider.selectByTenantId();

    assertTrue(sql.contains("FROM wf_form_binding"));
    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND deleted = 0"));
    assertTrue(sql.contains("ORDER BY business_module, process_key"));
    assertFalse(sql.contains("SELECT *"));
  }

  /**
   * 验证表单绑定保存 SQL 使用租户和流程 key 作为幂等更新键。
   */
  @Test
  void shouldGenerateUpsertSqlWithTenantAndProcessKey() {
    WorkflowFormBindingSqlProvider provider = new WorkflowFormBindingSqlProvider();

    String sql = provider.upsertBinding();

    assertTrue(sql.contains("INSERT INTO wf_form_binding"));
    assertTrue(sql.contains("tenant_id"));
    assertTrue(sql.contains("process_key"));
    assertTrue(sql.contains("business_module"));
    assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    assertTrue(sql.contains("form_route = VALUES(form_route)"));
  }
}
