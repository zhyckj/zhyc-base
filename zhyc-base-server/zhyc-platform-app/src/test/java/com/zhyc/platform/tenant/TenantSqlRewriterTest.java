/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * MyBatis 租户 SQL 重写器测试。
 */
class TenantSqlRewriterTest {

  /** 租户 SQL 重写器，覆盖首期核心租户业务表。 */
  private final TenantSqlRewriter rewriter = new TenantSqlRewriter(Set.of("sys_user", "pur_request"));

  /**
   * 验证租户表查询在已有 WHERE 条件前追加租户条件。
   */
  @Test
  void shouldAppendTenantConditionToSelectWithWhere() {
    String sql = "SELECT id, username FROM sys_user WHERE deleted = 0 ORDER BY id DESC";

    String rewritten = rewriter.rewrite(sql, "tenant-a");

    assertEquals("SELECT id, username FROM sys_user WHERE tenant_id = 'tenant-a' AND deleted = 0 ORDER BY id DESC",
        rewritten);
  }

  /**
   * 验证租户表查询没有 WHERE 条件时自动创建租户条件。
   */
  @Test
  void shouldCreateTenantConditionForSelectWithoutWhere() {
    String sql = "SELECT id, request_no FROM pur_request ORDER BY id DESC";

    String rewritten = rewriter.rewrite(sql, "tenant-a");

    assertEquals("SELECT id, request_no FROM pur_request WHERE tenant_id = 'tenant-a' ORDER BY id DESC", rewritten);
  }

  /**
   * 验证租户表更新会补充租户条件，避免跨租户批量更新。
   */
  @Test
  void shouldAppendTenantConditionToUpdate() {
    String sql = "UPDATE sys_user SET status = 'disabled' WHERE id = #{id}";

    String rewritten = rewriter.rewrite(sql, "tenant-a");

    assertEquals("UPDATE sys_user SET status = 'disabled' WHERE tenant_id = 'tenant-a' AND id = #{id}", rewritten);
  }

  /**
   * 验证租户表删除会补充租户条件，避免跨租户批量删除。
   */
  @Test
  void shouldAppendTenantConditionToDelete() {
    String sql = "DELETE FROM pur_request WHERE id = #{id}";

    String rewritten = rewriter.rewrite(sql, "tenant-a");

    assertEquals("DELETE FROM pur_request WHERE tenant_id = 'tenant-a' AND id = #{id}", rewritten);
  }

  /**
   * 验证已有租户条件的 SQL 不重复追加租户条件。
   */
  @Test
  void shouldSkipSqlWhenTenantConditionAlreadyExists() {
    String sql = "SELECT id FROM sys_user WHERE tenant_id = #{tenantId} AND deleted = 0";

    String rewritten = rewriter.rewrite(sql, "tenant-a");

    assertEquals(sql, rewritten);
  }

  /**
   * 验证全局表和未绑定租户上下文时不改写 SQL。
   */
  @Test
  void shouldSkipGlobalTableAndBlankTenant() {
    assertEquals("SELECT id FROM sys_tenant ORDER BY id DESC",
        rewriter.rewrite("SELECT id FROM sys_tenant ORDER BY id DESC", "tenant-a"));
    assertEquals("SELECT id FROM sys_user ORDER BY id DESC",
        rewriter.rewrite("SELECT id FROM sys_user ORDER BY id DESC", " "));
  }

  /**
   * 验证租户字面量会转义单引号，避免上下文异常值破坏 SQL 结构。
   */
  @Test
  void shouldEscapeTenantLiteral() {
    String rewritten = rewriter.rewrite("SELECT id FROM sys_user", "tenant'a");

    assertEquals("SELECT id FROM sys_user WHERE tenant_id = 'tenant''a'", rewritten);
  }
}
