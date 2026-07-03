/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * 低代码数据库方言枚举测试。
 */
class LowcodeDatabaseDialectTest {

  /**
   * 验证数据库方言编码稳定，供数据源、DDL 生成器、字段映射和分页方言统一路由。
   */
  @Test
  void shouldExposeStableDatabaseDialectCode() {
    assertEquals("mysql", LowcodeDatabaseDialect.MYSQL.getCode());
    assertEquals("MySQL", LowcodeDatabaseDialect.MYSQL.getLabel());
    assertEquals("postgresql", LowcodeDatabaseDialect.POSTGRESQL.getCode());
    assertEquals("PostgreSQL", LowcodeDatabaseDialect.POSTGRESQL.getLabel());
    assertEquals("oracle", LowcodeDatabaseDialect.ORACLE.getCode());
    assertEquals("Oracle", LowcodeDatabaseDialect.ORACLE.getLabel());
    assertEquals("sqlserver", LowcodeDatabaseDialect.SQLSERVER.getCode());
    assertEquals("SQL Server", LowcodeDatabaseDialect.SQLSERVER.getLabel());
    assertEquals("dm", LowcodeDatabaseDialect.DM.getCode());
    assertEquals("达梦数据库", LowcodeDatabaseDialect.DM.getLabel());
  }

  /**
   * 验证数据库方言可从持久化编码解析，未知编码必须明确拒绝。
   */
  @Test
  void shouldParseDatabaseDialectFromCode() {
    assertEquals(LowcodeDatabaseDialect.MYSQL, LowcodeDatabaseDialect.fromCode(" mysql "));
    assertEquals(LowcodeDatabaseDialect.POSTGRESQL, LowcodeDatabaseDialect.fromCode("PostgreSQL"));
    assertEquals(LowcodeDatabaseDialect.ORACLE, LowcodeDatabaseDialect.fromCode(" ORACLE "));
    assertEquals(LowcodeDatabaseDialect.SQLSERVER, LowcodeDatabaseDialect.fromCode("sqlserver"));
    assertEquals(LowcodeDatabaseDialect.DM, LowcodeDatabaseDialect.fromCode(" DM "));

    assertThrows(IllegalArgumentException.class, () -> LowcodeDatabaseDialect.fromCode("sqlite"));
  }
}
