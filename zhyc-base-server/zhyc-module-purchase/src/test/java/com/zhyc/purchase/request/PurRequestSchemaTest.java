/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * 采购申请数据库结构测试。
 */
class PurRequestSchemaTest {

  /**
   * 验证采购申请主表 DDL 包含租户隔离、唯一约束、常用查询索引和中文注释。
   *
   * @throws IOException 读取 DDL 失败时抛出
   */
  @Test
  void shouldDefinePurRequestSchema() throws IOException {
    String ddl = new String(Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("db/V1__purchase_core.sql").readAllBytes(), StandardCharsets.UTF_8);

    assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS pur_request"));
    assertTrue(ddl.contains("tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码'"));
    assertTrue(ddl.contains("request_no VARCHAR(64) NOT NULL COMMENT '采购申请单号'"));
    assertTrue(ddl.contains("process_status VARCHAR(32) NOT NULL COMMENT '流程状态'"));
    assertTrue(ddl.contains("process_instance_id VARCHAR(128) NULL COMMENT '流程实例 ID'"));
    assertTrue(ddl.contains("UNIQUE KEY uk_pur_request_tenant_no (tenant_id, request_no)"));
    assertTrue(ddl.contains("KEY idx_pur_request_tenant_applicant (tenant_id, applicant_id)"));
    assertTrue(ddl.contains("KEY idx_pur_request_tenant_status (tenant_id, process_status)"));
    assertTrue(ddl.contains("KEY idx_pur_request_tenant_process (tenant_id, process_instance_id)"));
    assertTrue(ddl.contains("COMMENT='采购申请主表'"));
  }

  /**
   * 验证采购申请状态查询开放 API 注册到开放平台运行态路由。
   *
   * @throws IOException 读取 DDL 失败时抛出
   */
  @Test
  void shouldRegisterPurRequestStatusOpenApiRoute() throws IOException {
    String ddl = new String(Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("db/V1__purchase_core.sql").readAllBytes(), StandardCharsets.UTF_8);

    assertTrue(ddl.contains("INSERT INTO openapi_catalog"));
    assertTrue(ddl.contains("'purchase-request-status'"));
    assertTrue(ddl.contains("'/openapi/v1/purchase/requests/{requestNo}/status'"));
    assertTrue(ddl.contains("INSERT INTO openapi_version"));
    assertTrue(ddl.contains("'http://zhyc-platform-app/openapi/v1/purchase/requests/{requestNo}/status'"));
    assertTrue(ddl.contains("ON DUPLICATE KEY UPDATE"));
  }
}
