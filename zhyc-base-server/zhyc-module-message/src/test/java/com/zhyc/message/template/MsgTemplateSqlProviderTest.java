/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template;

import com.zhyc.message.template.mapper.MsgTemplateSqlProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 消息模板 SQL Provider 测试。
 */
class MsgTemplateSqlProviderTest {

  /**
   * 验证消息模板查询 SQL 包含租户隔离和逻辑删除条件。
   */
  @Test
  void shouldGenerateTenantIsolatedQuerySql() {
    String sql = new MsgTemplateSqlProvider().selectByTenantId();

    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND deleted = 0"));
  }

  /**
   * 验证消息模板保存 SQL 使用租户和模板编码唯一键更新，不出现无条件更新。
   */
  @Test
  void shouldGenerateSafeUpsertSql() {
    String sql = new MsgTemplateSqlProvider().upsert();

    assertTrue(sql.contains("INSERT INTO msg_template"));
    assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
    assertTrue(sql.contains("version = version + 1"));
  }
}
