/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n;

import com.zhyc.i18n.mapper.I18nSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 国际化词条 SQL 生成测试。
 */
class I18nSqlProviderTest {

  /**
   * 验证词条查询 SQL 包含租户、语言、状态和逻辑删除条件。
   */
  @Test
  void shouldBuildMessageQueryWithTenantLocaleStatusAndDeletedFilter() {
    String sql = new I18nSqlProvider().selectMessages("tenant_a", "zh-CN", "enabled");

    assertTrue(sql.contains("FROM i18n_message"));
    assertTrue(sql.contains("tenant_id = #{tenantId}"));
    assertTrue(sql.contains("locale = #{locale}"));
    assertTrue(sql.contains("message_status = #{status}"));
    assertTrue(sql.contains("deleted = 0"));
  }

  /**
   * 验证词条解析 SQL 只查询启用词条。
   */
  @Test
  void shouldBuildResolveQueryWithEnabledStatus() {
    String sql = new I18nSqlProvider().selectEnabledMessage();

    assertTrue(sql.contains("message_key = #{messageKey}"));
    assertTrue(sql.contains("message_status = 'enabled'"));
  }
}
