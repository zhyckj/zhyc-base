/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhyc.purchase.request.mapper.PurRequestSqlProvider;
import org.junit.jupiter.api.Test;

/**
 * 采购申请 SQL Provider 测试。
 */
class PurRequestSqlProviderTest {

  /**
   * 验证采购申请状态查询 SQL 显式选择字段、包含租户隔离条件且不限制流程状态。
   */
  @Test
  void shouldBuildTenantScopedStatusQuerySql() {
    String sql = new PurRequestSqlProvider().selectByTenantIdAndRequestNo();

    assertTrue(sql.contains("tenant_id AS tenantId"));
    assertTrue(sql.contains("request_no AS requestNo"));
    assertTrue(sql.contains("process_status AS processStatus"));
    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND request_no = #{requestNo}"));
    assertFalse(sql.contains("AND process_status = 'DRAFT'"));
    assertFalse(sql.toUpperCase().contains("SELECT *"));
  }

  /**
   * 验证采购申请写入和提交 SQL 均带有明确字段和租户条件。
   */
  @Test
  void shouldBuildCreateAndSubmitSql() {
    PurRequestSqlProvider sqlProvider = new PurRequestSqlProvider();
    String insertSql = sqlProvider.insert();
    String submitSql = sqlProvider.updateSubmitted();

    assertTrue(insertSql.contains("INSERT INTO pur_request"));
    assertTrue(insertSql.contains("tenant_id, request_no, request_title"));
    assertTrue(insertSql.contains("process_status"));
    assertFalse(insertSql.toUpperCase().contains("SELECT *"));
    assertTrue(submitSql.contains("UPDATE pur_request"));
    assertTrue(submitSql.contains("process_instance_id = #{processInstanceId}"));
    assertTrue(submitSql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(submitSql.contains("AND request_no = #{requestNo}"));
    assertTrue(submitSql.contains("AND process_status = 'DRAFT'"));
    assertTrue(submitSql.contains("AND deleted = 0"));
  }

  /**
   * 验证流程状态更新 SQL 限定审批中状态，避免重复回调覆盖终态。
   */
  @Test
  void shouldBuildWorkflowStatusUpdateSqlWithApprovingGuard() {
    String sql = new PurRequestSqlProvider().updateProcessStatus();

    assertTrue(sql.contains("UPDATE pur_request"));
    assertTrue(sql.contains("process_status = #{processStatus}"));
    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND request_no = #{requestNo}"));
    assertTrue(sql.contains("AND process_status = 'APPROVING'"));
    assertTrue(sql.contains("AND deleted = 0"));
  }
}
