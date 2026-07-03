/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * 采购订单数据库结构测试。
 */
class PurOrderSchemaTest {

  /**
   * 验证采购订单主表和明细表包含租户隔离、业务唯一约束、常用索引和中文注释。
   *
   * @throws IOException 读取 DDL 失败时抛出
   */
  @Test
  void shouldDefinePurOrderSchema() throws IOException {
    String ddl = new String(Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("db/V1__purchase_core.sql").readAllBytes(), StandardCharsets.UTF_8);

    assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS pur_order"));
    assertTrue(ddl.contains("tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码'"));
    assertTrue(ddl.contains("order_no VARCHAR(64) NOT NULL COMMENT '采购订单号'"));
    assertTrue(ddl.contains("request_no VARCHAR(64) NOT NULL COMMENT '采购申请单号'"));
    assertTrue(ddl.contains("order_status VARCHAR(32) NOT NULL COMMENT '订单状态'"));
    assertTrue(ddl.contains("UNIQUE KEY uk_pur_order_tenant_no (tenant_id, order_no)"));
    assertTrue(ddl.contains("KEY idx_pur_order_tenant_request (tenant_id, request_no)"));
    assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS pur_order_item"));
    assertTrue(ddl.contains("order_no VARCHAR(64) NOT NULL COMMENT '采购订单号'"));
    assertTrue(ddl.contains("item_name VARCHAR(128) NOT NULL COMMENT '物品名称'"));
    assertTrue(ddl.contains("KEY idx_pur_order_item_tenant_order (tenant_id, order_no)"));
    assertTrue(ddl.contains("COMMENT='采购订单主表'"));
    assertTrue(ddl.contains("COMMENT='采购订单明细表'"));
  }

  /**
   * 验证采购订单详情开放 API 注册到开放平台运行态路由。
   *
   * @throws IOException 读取 DDL 失败时抛出
   */
  @Test
  void shouldRegisterPurOrderDetailOpenApiRoute() throws IOException {
    String ddl = new String(Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("db/V1__purchase_core.sql").readAllBytes(), StandardCharsets.UTF_8);

    assertTrue(ddl.contains("'purchase-order-detail'"));
    assertTrue(ddl.contains("'/openapi/v1/purchase/orders/{orderNo}'"));
    assertTrue(ddl.contains("'http://zhyc-platform-app/openapi/v1/purchase/orders/{orderNo}'"));
    assertTrue(ddl.contains("JSON_OBJECT('method', 'GET', 'tenantHeader', 'X-ZHYC-Tenant-Id'"));
  }
}
