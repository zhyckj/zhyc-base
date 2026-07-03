/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhyc.lowcode.db.LowcodeColumn;
import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.db.LowcodeTable;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * MySQL DDL 生成器测试。
 */
class MySqlDdlGeneratorTest {

  /**
   * 验证首期 MySQL 建表 DDL 会生成主键、自增、非空和注释。
   */
  @Test
  void generatesCreateTableSql() {
    LowcodeTable table = new LowcodeTable(
        "biz_order",
        "订单表",
        List.of(
            LowcodeColumn.builder("id", LowcodeFieldType.LONG)
                .comment("主键")
                .primaryKey(true)
                .autoIncrement(true)
                .nullable(false)
                .build(),
            LowcodeColumn.builder("order_no", LowcodeFieldType.STRING)
                .length(64)
                .comment("订单编号")
                .nullable(false)
                .build(),
            LowcodeColumn.builder("amount", LowcodeFieldType.DECIMAL)
                .length(18)
                .scale(2)
                .comment("订单金额")
                .nullable(false)
                .build()));

    String ddl = new MySqlDdlGenerator(new MySqlFieldTypeMapper()).generateCreateTable(table);

    assertEquals("""
        CREATE TABLE IF NOT EXISTS `biz_order` (
            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
            `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
            `amount` DECIMAL(18,2) NOT NULL COMMENT '订单金额',
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
        """.strip(), ddl);
  }

  /**
   * 验证租户业务表会生成租户和逻辑删除组合索引。
   */
  @Test
  void shouldGenerateTenantDeletedIndexWhenTenantColumnsExist() {
    LowcodeTable table = new LowcodeTable(
        "biz_order",
        "订单表",
        List.of(
            LowcodeColumn.builder("id", LowcodeFieldType.LONG)
                .comment("主键")
                .primaryKey(true)
                .autoIncrement(true)
                .nullable(false)
                .build(),
            LowcodeColumn.builder("tenant_id", LowcodeFieldType.STRING)
                .length(64)
                .comment("租户业务编码")
                .nullable(false)
                .build(),
            LowcodeColumn.builder("deleted", LowcodeFieldType.BOOLEAN)
                .comment("逻辑删除标识")
                .nullable(false)
                .build()));

    String ddl = new MySqlDdlGenerator(new MySqlFieldTypeMapper()).generateCreateTable(table);

    assertEquals("""
        CREATE TABLE IF NOT EXISTS `biz_order` (
            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
            `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户业务编码',
            `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
            PRIMARY KEY (`id`),
            KEY `idx_biz_order_tenant_deleted` (`tenant_id`, `deleted`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
        """.strip(), ddl);
  }

  /**
   * 验证通用审计字段会生成首期默认值，避免业务插入时遗漏非空字段。
   */
  @Test
  void shouldGenerateDefaultClausesForCommonColumns() {
    LowcodeTable table = new LowcodeTable(
        "biz_order",
        "订单表",
        List.of(
            LowcodeColumn.builder("id", LowcodeFieldType.LONG)
                .comment("主键")
                .primaryKey(true)
                .autoIncrement(true)
                .nullable(false)
                .build(),
            LowcodeColumn.builder("created_at", LowcodeFieldType.DATETIME)
                .comment("创建时间")
                .nullable(false)
                .build(),
            LowcodeColumn.builder("updated_at", LowcodeFieldType.DATETIME)
                .comment("最后更新时间")
                .nullable(false)
                .build(),
            LowcodeColumn.builder("deleted", LowcodeFieldType.BOOLEAN)
                .comment("逻辑删除标识")
                .nullable(false)
                .build(),
            LowcodeColumn.builder("version", LowcodeFieldType.INTEGER)
                .comment("乐观锁版本号")
                .nullable(false)
                .build()));

    String ddl = new MySqlDdlGenerator(new MySqlFieldTypeMapper()).generateCreateTable(table);

    assertEquals("""
        CREATE TABLE IF NOT EXISTS `biz_order` (
            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
            `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
            `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
            `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
        """.strip(), ddl);
  }
}
