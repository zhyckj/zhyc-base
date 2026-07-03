/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import com.zhyc.lowcode.db.mysql.MySqlFieldTypeMapper;
import com.zhyc.lowcode.db.mysql.MySqlPaginationDialect;
import com.zhyc.lowcode.db.mysql.MySqlDdlGenerator;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 低代码数据库方言注册中心测试。
 */
class LowcodeDbDialectRegistryTest {

  /**
   * 验证同一数据库方言可被按大小写和空白归一化后命中各类能力。
   */
  @Test
  void supportsMysqlCapabilitiesAfterDialectNormalization() {
    MySqlDdlGenerator ddlGenerator = new MySqlDdlGenerator(new MySqlFieldTypeMapper());
    MySqlFieldTypeMapper fieldTypeMapper = new MySqlFieldTypeMapper();
    MySqlPaginationDialect paginationDialect = new MySqlPaginationDialect();
    LowcodeDbDialectRegistry registry = new LowcodeDbDialectRegistry(
        List.of(ddlGenerator),
        List.of(fieldTypeMapper),
        List.of(paginationDialect));

    assertSame(ddlGenerator, registry.getDdlGenerator(" MySQL "));
    assertSame(fieldTypeMapper, registry.getFieldTypeMapper("mysql"));
    assertSame(paginationDialect, registry.getPaginationDialect("MYSQL"));
  }

  /**
   * 验证注册中心可输出三类方言能力清单，供后台展示和扩展评审使用。
   */
  @Test
  void shouldListRegisteredDialectCapabilities() {
    MySqlDdlGenerator ddlGenerator = new MySqlDdlGenerator(new MySqlFieldTypeMapper());
    MySqlFieldTypeMapper fieldTypeMapper = new MySqlFieldTypeMapper();
    MySqlPaginationDialect paginationDialect = new MySqlPaginationDialect();
    LowcodeDbDialectRegistry registry = new LowcodeDbDialectRegistry(
        List.of(ddlGenerator),
        List.of(fieldTypeMapper),
        List.of(paginationDialect));

    assertTrue(registry.listDdlDialectCodes().contains("mysql"));
    assertTrue(registry.listFieldTypeDialectCodes().contains("mysql"));
    assertTrue(registry.listPaginationDialectCodes().contains("mysql"));
  }

  /**
   * 验证未注册的数据库方言会返回明确的失败边界。
   */
  @Test
  void rejectsUnknownDialect() {
    LowcodeDbDialectRegistry registry = new LowcodeDbDialectRegistry(
        List.of(),
        List.of(),
        List.of());

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> registry.getDdlGenerator("postgres"));
    assertTrue(ex.getMessage().contains("DDL 生成器不支持该数据库方言"));
  }

  /**
   * 验证重复注册会在启动期失败，避免运行期路由不确定。
   */
  @Test
  void rejectsDuplicateDialectRegistrationForDdlGenerator() {
    MySqlFieldTypeMapper fieldTypeMapper = new MySqlFieldTypeMapper();
    MySqlPaginationDialect paginationDialect = new MySqlPaginationDialect();
    DuplicateMySqlDdlGenerator first = new DuplicateMySqlDdlGenerator(fieldTypeMapper);
    DuplicateMySqlDdlGenerator second = new DuplicateMySqlDdlGenerator(fieldTypeMapper);

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> new LowcodeDbDialectRegistry(
            List.of(first, second),
            List.of(fieldTypeMapper),
            List.of(paginationDialect)));
    assertTrue(ex.getMessage().contains("DDL 生成器重复注册数据库方言"));
  }

  /**
   * 测试专用重复方言 DDL 生成器。
   */
  private static class DuplicateMySqlDdlGenerator extends MySqlDdlGenerator {
    private DuplicateMySqlDdlGenerator(MySqlFieldTypeMapper fieldTypeMapper) {
      super(fieldTypeMapper);
    }

    @Override
    public String getDialectName() {
      return " MYSQL ";
    }
  }
}
