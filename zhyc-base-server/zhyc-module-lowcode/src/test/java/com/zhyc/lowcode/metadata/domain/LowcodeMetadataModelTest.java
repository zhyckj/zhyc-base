/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

import com.zhyc.lowcode.db.LowcodeFieldType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 低代码元数据领域模型测试。
 */
class LowcodeMetadataModelTest {

  /**
   * 验证数据源定义会规范化基础连接信息，便于后续按租户和方言查找。
   */
  @Test
  void shouldNormalizeDataSourceDefinition() {
    LowcodeDataSource dataSource = new LowcodeDataSource(
        1L, " tenant_a ", " main_mysql ", "主库", LowcodeDatabaseDialect.MYSQL,
        "jdbc:mysql://127.0.0.1:3306/zhyc", "root", true);

    assertEquals("tenant_a", dataSource.getTenantId());
    assertEquals("main_mysql", dataSource.getCode());
    assertEquals(LowcodeDatabaseDialect.MYSQL, dataSource.getDialect());
    assertTrue(dataSource.isEnabled());
  }

  /**
   * 验证数据源定义保留密钥引用，避免数据库密码明文进入领域对象。
   */
  @Test
  void shouldKeepDataSourcePasswordSecretReference() {
    LowcodeDataSource dataSource = new LowcodeDataSource(
        1L, "tenant_a", "main_mysql", "主库", LowcodeDatabaseDialect.MYSQL,
        "jdbc:mysql://127.0.0.1:3306/zhyc", "root", " secret:lowcode:main ", true);

    assertEquals("secret:lowcode:main", dataSource.getPasswordSecretRef());
  }

  /**
   * 验证表模型可还原数据源主键和发布状态，供数据库仓储查询后恢复完整元数据。
   */
  @Test
  void shouldRestoreTableModelDataSourceAndStatus() {
    LowcodeTableModel table = new LowcodeTableModel(
        1L, "tenant_a", 10L, "purchase_order", "采购订单", "pur_order",
        LowcodeModelStatus.PUBLISHED,
        List.of(LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG).primaryKey(true).build()));

    assertEquals(10L, table.getDataSourceId());
    assertEquals(LowcodeModelStatus.PUBLISHED, table.getStatus());
  }

  /**
   * 验证表模型要求字段编码唯一，避免生成重复列、重复表单项和重复接口字段。
   */
  @Test
  void shouldRejectDuplicateColumnCodeInTableModel() {
    LowcodeTableModel table = new LowcodeTableModel(
        1L, "tenant_a", "purchase_order", "采购订单", "pur_order",
        List.of(
            LowcodeColumnModel.builder("order_no", "订单号", LowcodeFieldType.STRING).length(64).build(),
            LowcodeColumnModel.builder("order_no", "订单编号", LowcodeFieldType.STRING).length(64).build()
        ));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, table::validate);

    assertEquals("字段编码不能重复: order_no", exception.getMessage());
  }

  /**
   * 验证发布表模型时必须存在主键字段，确保后端接口和前端编辑页有稳定业务定位。
   */
  @Test
  void shouldRequirePrimaryKeyColumnWhenPublishing() {
    LowcodeTableModel table = new LowcodeTableModel(
        1L, "tenant_a", "purchase_order", "采购订单", "pur_order",
        List.of(LowcodeColumnModel.builder("order_no", "订单号", LowcodeFieldType.STRING).length(64).build()));

    assertFalse(table.hasPrimaryKey());

    IllegalStateException exception = assertThrows(IllegalStateException.class, table::publish);

    assertEquals("发布表模型前必须配置主键字段", exception.getMessage());
  }

  /**
   * 验证字段建模保留列表、表单、查询配置，供 Vue、UniApp 和开放 API 模板复用。
   */
  @Test
  void shouldKeepUiAndQueryFlagsInColumnModel() {
    LowcodeColumnModel column = LowcodeColumnModel.builder("amount", "订单金额", LowcodeFieldType.DECIMAL)
        .length(18)
        .scale(2)
        .required(true)
        .listVisible(true)
        .formVisible(true)
        .queryable(true)
        .dictCode("order_status")
        .build();

    assertTrue(column.isRequired());
    assertTrue(column.isListVisible());
    assertTrue(column.isFormVisible());
    assertTrue(column.isQueryable());
    assertEquals("order_status", column.getDictCode());
    assertEquals(18, column.getLength());
    assertEquals(2, column.getScale());
  }
}
