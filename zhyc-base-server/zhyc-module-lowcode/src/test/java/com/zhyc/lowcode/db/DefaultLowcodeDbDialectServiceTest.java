/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.lowcode.db.mysql.MySqlDdlGenerator;
import com.zhyc.lowcode.db.mysql.MySqlFieldTypeMapper;
import com.zhyc.lowcode.db.mysql.MySqlPaginationDialect;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 低代码数据库方言统一服务测试。
 */
class DefaultLowcodeDbDialectServiceTest {

  /**
   * 验证服务会按数据库方言生成建表 SQL。
   */
  @Test
  void shouldGenerateCreateTableUsingMysqlDialect() {
    LowcodeDbDialectService service = createMysqlService();
    LowcodeTable table = new LowcodeTable(
        "biz_user",
        "用户表",
        List.of(
            LowcodeColumn.builder("id", LowcodeFieldType.LONG)
                .primaryKey(true)
                .autoIncrement(true)
                .nullable(false)
                .comment("主键")
                .build(),
            LowcodeColumn.builder("username", LowcodeFieldType.STRING)
                .length(32)
                .nullable(false)
                .comment("用户名")
                .build()));

    String ddl = service.generateCreateTable("mysql", table);

    assertEquals("""
        CREATE TABLE IF NOT EXISTS `biz_user` (
            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
            `username` VARCHAR(32) NOT NULL COMMENT '用户名',
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';""".strip(), ddl);
  }

  /**
   * 验证建表模型为空时返回稳定业务错误码，避免向接口调用方暴露空指针异常。
   */
  @Test
  void shouldRejectNullTableWhenGeneratingCreateTable() {
    LowcodeDbDialectService service = createMysqlService();

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.generateCreateTable("mysql", null));

    assertEquals("ZHYC_LOWCODE_DIALECT_TABLE_REQUIRED", exception.getCode());
    assertEquals("建表模型不能为空", exception.getMessage());
  }

  /**
   * 验证服务可调用字段映射能力。
   */
  @Test
  void shouldMapFieldTypeByDialect() {
    LowcodeDbDialectService service = createMysqlService();
    LowcodeColumn column = LowcodeColumn.builder("amount", LowcodeFieldType.DECIMAL)
        .length(18)
        .scale(2)
        .build();

    assertEquals("DECIMAL(18,2)", service.mapFieldType(" MYSQL ", column));
  }

  /**
   * 验证字段模型为空时返回稳定业务错误码，避免向接口调用方暴露空指针异常。
   */
  @Test
  void shouldRejectNullColumnWhenMappingFieldType() {
    LowcodeDbDialectService service = createMysqlService();

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.mapFieldType("mysql", null));

    assertEquals("ZHYC_LOWCODE_DIALECT_COLUMN_REQUIRED", exception.getCode());
    assertEquals("字段模型不能为空", exception.getMessage());
  }

  /**
   * 验证服务可按方言追加分页语法。
   */
  @Test
  void shouldApplyPaginationByDialect() {
    LowcodeDbDialectService service = createMysqlService();

    assertEquals("SELECT * FROM demo LIMIT 20, 10",
        service.applyPagination("MySQL", "SELECT * FROM demo", 20, 10));
  }

  /**
   * 验证分页 SQL 为空时返回稳定业务错误码，避免对接口调用方暴露裸参数异常。
   */
  @Test
  void shouldRejectBlankSqlWhenApplyingPagination() {
    LowcodeDbDialectService service = createMysqlService();

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.applyPagination("mysql", " ", 0, 10));

    assertEquals("ZHYC_LOWCODE_DIALECT_SQL_REQUIRED", exception.getCode());
    assertEquals("SQL 不能为空", exception.getMessage());
  }

  /**
   * 验证服务可输出三类方言能力清单，供接口层和后台页面统一展示。
   */
  @Test
  void shouldListDialectCapabilities() {
    LowcodeDbDialectService service = createMysqlService();

    assertEquals(List.of("mysql"), service.listDdlDialectCodes());
    assertEquals(List.of("mysql"), service.listFieldTypeDialectCodes());
    assertEquals(List.of("mysql"), service.listPaginationDialectCodes());
  }

  /**
   * 验证未知字段映射方言会返回稳定业务错误码。
   */
  @Test
  void shouldRejectUnknownDialectWhenMappingFieldTypeWithBusinessCode() {
    LowcodeDbDialectService service = createMysqlService();

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.mapFieldType("PostgreSQL",
            LowcodeColumn.builder("name", LowcodeFieldType.STRING).length(16).build()));

    assertEquals("ZHYC_LOWCODE_DIALECT_FIELD_TYPE_UNSUPPORTED", exception.getCode());
    assertEquals("字段类型映射器不支持该数据库方言: postgresql", exception.getMessage());
  }

  /**
   * 验证未知 DDL 方言会返回稳定业务错误码。
   */
  @Test
  void shouldRejectUnknownDialectWhenGeneratingCreateTableWithBusinessCode() {
    LowcodeDbDialectService service = createMysqlService();
    LowcodeTable table = new LowcodeTable(
        "biz_user",
        "用户表",
        List.of(LowcodeColumn.builder("id", LowcodeFieldType.LONG)
            .primaryKey(true)
            .nullable(false)
            .comment("主键")
            .build()));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.generateCreateTable("PostgreSQL", table));

    assertEquals("ZHYC_LOWCODE_DIALECT_DDL_UNSUPPORTED", exception.getCode());
    assertEquals("DDL 生成器不支持该数据库方言: postgresql", exception.getMessage());
  }

  /**
   * 验证未知分页方言会返回稳定业务错误码。
   */
  @Test
  void shouldRejectUnknownDialectWhenApplyingPaginationWithBusinessCode() {
    LowcodeDbDialectService service = createMysqlService();

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.applyPagination("PostgreSQL", "SELECT 1", 0, 10));

    assertEquals("ZHYC_LOWCODE_DIALECT_PAGINATION_UNSUPPORTED", exception.getCode());
    assertEquals("分页方言不支持该数据库方言: postgresql", exception.getMessage());
  }

  /**
   * 验证数据库方言编码为空时返回稳定业务错误码。
   */
  @Test
  void shouldRejectBlankDialectCodeWithBusinessCode() {
    LowcodeDbDialectService service = createMysqlService();

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.applyPagination(" ", "SELECT 1", 0, 10));

    assertEquals("ZHYC_LOWCODE_DIALECT_CODE_REQUIRED", exception.getCode());
    assertEquals("数据库方言编码不能为空", exception.getMessage());
  }

  private static LowcodeDbDialectService createMysqlService() {
    MySqlFieldTypeMapper mySqlFieldTypeMapper = new MySqlFieldTypeMapper();
    MySqlDdlGenerator mySqlDdlGenerator = new MySqlDdlGenerator(mySqlFieldTypeMapper);
    LowcodeDbDialectRegistry registry = new LowcodeDbDialectRegistry(
        List.of(mySqlDdlGenerator),
        List.of(mySqlFieldTypeMapper),
        List.of(new MySqlPaginationDialect()));
    return new DefaultLowcodeDbDialectService(registry);
  }
}
