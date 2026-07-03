/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category;

import com.zhyc.workflow.category.mapper.WorkflowCategorySqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流分类 SQL Provider 测试。
 */
class WorkflowCategorySqlProviderTest {

  /**
   * 验证分类查询 SQL 包含租户隔离、逻辑删除过滤和稳定排序。
   */
  @Test
  void shouldGenerateListSqlWithTenantIsolation() {
    WorkflowCategorySqlProvider provider = new WorkflowCategorySqlProvider();

    String sql = provider.selectByTenantId();

    assertTrue(sql.contains("FROM wf_category"));
    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND deleted = 0"));
    assertTrue(sql.contains("ORDER BY sort_order, id"));
    assertFalse(sql.contains("SELECT *"));
  }

  /**
   * 验证分类保存 SQL 覆盖首期必需字段，并使用租户加分类编码作为更新条件。
   */
  @Test
  void shouldGenerateUpsertSqlWithTenantAndCategoryCode() {
    WorkflowCategorySqlProvider provider = new WorkflowCategorySqlProvider();

    String sql = provider.upsertCategory();

    assertTrue(sql.contains("INSERT INTO wf_category"));
    assertTrue(sql.contains("tenant_id"));
    assertTrue(sql.contains("category_code"));
    assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    assertTrue(sql.contains("category_name = VALUES(category_name)"));
    assertTrue(sql.contains("updated_at = CURRENT_TIMESTAMP"));
  }
}
