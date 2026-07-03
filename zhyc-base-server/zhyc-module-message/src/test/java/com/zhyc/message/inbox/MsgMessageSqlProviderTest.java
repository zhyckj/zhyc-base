/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox;

import com.zhyc.message.inbox.mapper.MsgMessageSqlProvider;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 站内消息 SQL Provider 测试。
 */
class MsgMessageSqlProviderTest {

  /**
   * 验证消息分页 SQL 包含租户、接收人和已读状态条件。
   */
  @Test
  void shouldGenerateTenantReceiverPageSql() {
    String sql = new MsgMessageSqlProvider().selectPageByQuery(Map.of("readFlag", false));

    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND receiver_id = #{receiverId}"));
    assertTrue(sql.contains("AND read_flag = #{readFlag}"));
    assertTrue(sql.contains("LIMIT #{pageSize} OFFSET #{offset}"));
  }

  /**
   * 验证标记已读 SQL 按租户、消息编码和接收人限定更新范围。
   */
  @Test
  void shouldGenerateSafeMarkReadSql() {
    String sql = new MsgMessageSqlProvider().markRead();

    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND message_code = #{messageCode}"));
    assertTrue(sql.contains("AND receiver_id = #{receiverId}"));
    assertTrue(sql.contains("AND deleted = 0"));
  }
}
