/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search;

import com.zhyc.search.mapper.SearchSqlProvider;
import com.zhyc.search.repository.SearchRuntimeQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 全文检索 SQL 生成测试。
 */
class SearchSqlProviderTest {

  /**
   * 验证索引配置查询 SQL 包含租户、状态和逻辑删除条件。
   */
  @Test
  void shouldBuildIndexConfigQueryWithTenantStatusAndDeletedFilter() {
    String sql = new SearchSqlProvider().selectIndexConfigs("tenant_a", "enabled");

    assertTrue(sql.contains("FROM search_index_config"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("index_status = #{status}"));
    assertTrue(sql.contains("deleted = 0"));
  }

  /**
   * 验证重建任务查询 SQL 包含租户和索引编码过滤。
   */
  @Test
  void shouldBuildRebuildTaskQueryWithTenantAndIndexCodeFilter() {
    String sql = new SearchSqlProvider().selectRebuildTasks("tenant_a", "cms_content");

    assertTrue(sql.contains("FROM search_rebuild_task"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("index_code = #{indexCode}"));
    assertTrue(sql.contains("deleted = 0"));
  }

  /**
   * 验证搜索日志查询 SQL 包含租户和索引编码过滤。
   */
  @Test
  void shouldBuildQueryLogQueryWithTenantAndIndexCodeFilter() {
    String sql = new SearchSqlProvider().selectQueryLogs("tenant_a", "cms_content");

    assertTrue(sql.contains("FROM search_query_log"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("index_code = #{indexCode}"));
  }

  /**
   * 验证运行时检索 SQL 使用服务层校验后的来源表和检索表达式。
   */
  @Test
  void shouldBuildRuntimeSearchQueryWithValidatedExpressions() {
    String sql = new SearchSqlProvider().selectSearchItems(new SearchRuntimeQuery("tenant_a", "cms_content",
        "COALESCE(CAST(title AS CHAR), '')",
        "title LIKE CONCAT('%', #{keyword}, '%')", "采购制度", 20));

    assertTrue(sql.contains("FROM ${sourceTable}"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("deleted = 0"));
    assertTrue(sql.contains("AND (${whereExpression})"));
    assertTrue(sql.contains("LIMIT #{limit}"));
  }
}
