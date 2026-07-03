/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * 站内消息数据库结构测试。
 */
class MsgMessageSchemaTest {

  /**
   * 验证站内消息表包含租户隔离、唯一约束、阅读状态和更新审计字段。
   *
   * @throws IOException 读取 DDL 失败时抛出
   */
  @Test
  void shouldDefineMsgMessageSchema() throws IOException {
    String ddl = new String(Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("db/V1__message_core.sql").readAllBytes(), StandardCharsets.UTF_8);
    String tableDdl = ddl.substring(ddl.indexOf("CREATE TABLE IF NOT EXISTS msg_message"));

    assertTrue(tableDdl.contains("CREATE TABLE IF NOT EXISTS msg_message"));
    assertTrue(tableDdl.contains("tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码'"));
    assertTrue(tableDdl.contains("message_code VARCHAR(64) NOT NULL COMMENT '消息编码'"));
    assertTrue(tableDdl.contains("read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读，0 未读，1 已读'"));
    assertTrue(tableDdl.contains("read_at DATETIME NULL COMMENT '阅读时间'"));
    assertTrue(tableDdl.contains(
        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'"));
    assertTrue(tableDdl.contains("UNIQUE KEY uk_msg_message_tenant_code (tenant_id, message_code)"));
    assertTrue(tableDdl.contains("KEY idx_msg_message_tenant_receiver (tenant_id, receiver_id, read_flag, created_at)"));
    assertTrue(tableDdl.contains("COMMENT='站内消息表'"));
  }
}
