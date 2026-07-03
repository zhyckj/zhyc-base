/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.converter;

import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.db.LowcodeTable;
import com.zhyc.lowcode.db.mysql.MySqlDdlGenerator;
import com.zhyc.lowcode.db.mysql.MySqlFieldTypeMapper;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 低代码表模型转换器测试。
 */
class LowcodeTableModelConverterTest {

  /**
   * 验证表模型可转换为 DDL 生成器使用的数据表结构。
   */
  @Test
  void shouldConvertTableModelToDdlTable() {
    LowcodeTableModel tableModel = new LowcodeTableModel(
        1L, "tenant_a", "purchase_order", "采购订单", "pur_order",
        List.of(
            LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
                .primaryKey(true)
                .autoIncrement(true)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("order_no", "订单编号", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
                .build()));

    LowcodeTable table = new LowcodeTableModelConverter().toDdlTable(tableModel);
    String ddl = new MySqlDdlGenerator(new MySqlFieldTypeMapper()).generateCreateTable(table);

    assertEquals("""
        CREATE TABLE IF NOT EXISTS `pur_order` (
            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
            `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户业务编码，用于共享表模式下的数据隔离',
            `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
            `created_by` BIGINT NULL COMMENT '创建人用户ID',
            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
            `updated_by` BIGINT NULL COMMENT '最后更新人用户ID',
            `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
            `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
            `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
            `remark` VARCHAR(500) NULL COMMENT '备注',
            PRIMARY KEY (`id`),
            KEY `idx_pur_order_tenant_deleted` (`tenant_id`, `deleted`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单';
        """.strip(), ddl);
  }
}
