/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms;

import com.zhyc.cms.mapper.CmsSqlProvider;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内容管理 SQL Provider 测试。
 */
class CmsSqlProviderTest {

  /**
   * 验证栏目查询 SQL 包含租户隔离、状态筛选和逻辑删除条件。
   */
  @Test
  void shouldBuildTenantScopedChannelQuerySql() {
    String sql = new CmsSqlProvider().selectChannels(Map.of("status", "enabled"));

    assertTrue(sql.contains("FROM cms_channel"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("deleted = 0"));
    assertTrue(sql.contains("channel_status = #{status}"));
  }

  /**
   * 验证文章查询 SQL 包含租户隔离、栏目筛选和状态筛选。
   */
  @Test
  void shouldBuildTenantScopedContentQuerySql() {
    String sql = new CmsSqlProvider().selectContents(Map.of("channelCode", "news", "status", "published"));

    assertTrue(sql.contains("FROM cms_content"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("channel_code = #{channelCode}"));
    assertTrue(sql.contains("content_status = #{status}"));
  }

  /**
   * 验证文章更新 SQL 必须限定租户、主键和逻辑删除状态。
   */
  @Test
  void shouldBuildTenantScopedContentUpdateSql() {
    String sql = new CmsSqlProvider().updateContent();

    assertTrue(sql.contains("UPDATE cms_content"));
    assertTrue(sql.contains("channel_code = #{channelCode}"));
    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND id = #{id}"));
    assertTrue(sql.contains("AND deleted = 0"));
  }
}
