/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * JDBC 平台用户查询服务测试。
 */
class JdbcPlatformUserDetailsServiceTest {

  /**
   * 验证认证中心读取平台用户密码哈希，后台重置密码后只接受新密码。
   */
  @Test
  void shouldUseLatestPlatformUserPasswordHash() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createUserTable(jdbcTemplate);
    ShiroPasswordEncoder encoder = new ShiroPasswordEncoder();
    jdbcTemplate.update("""
        INSERT INTO sys_user (tenant_id, username, password_hash, status)
        VALUES (?, ?, ?, ?)
        """, "zhyc-platform", "admin", encoder.encode("old-password"), "enabled");
    JdbcPlatformUserDetailsService userDetailsService =
        new JdbcPlatformUserDetailsService(jdbcTemplate, "zhyc-platform");

    UserDetails oldUser = userDetailsService.loadUserByUsername("admin");
    assertTrue(encoder.matches("old-password", oldUser.getPassword()));

    jdbcTemplate.update("""
        UPDATE sys_user
        SET password_hash = ?
        WHERE tenant_id = ? AND username = ?
        """, encoder.encode("new-password"), "zhyc-platform", "admin");

    UserDetails newUser = userDetailsService.loadUserByUsername("admin");
    assertTrue(encoder.matches("new-password", newUser.getPassword()));
    assertFalse(encoder.matches("old-password", newUser.getPassword()));
  }

  /**
   * 验证不存在的用户会按 Spring Security 约定抛出异常。
   */
  @Test
  void shouldRejectUnknownUser() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    createUserTable(jdbcTemplate);
    JdbcPlatformUserDetailsService userDetailsService =
        new JdbcPlatformUserDetailsService(jdbcTemplate, "zhyc-platform");

    assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("admin"));
  }

  /**
   * 创建测试用户表。
   *
   * @param jdbcTemplate JDBC 操作模板
   */
  private void createUserTable(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
        CREATE TABLE sys_user (
          tenant_id VARCHAR(64) NOT NULL,
          username VARCHAR(64) NOT NULL,
          password_hash VARCHAR(255) NOT NULL,
          status VARCHAR(32) NOT NULL
        )
        """);
  }

  /**
   * 创建 H2 JDBC 操作模板。
   *
   * @return JDBC 操作模板
   */
  private JdbcTemplate createJdbcTemplate() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:auth_platform_user_" + System.nanoTime()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "sa", "");
    dataSource.setDriverClassName("org.h2.Driver");
    return new JdbcTemplate(dataSource);
  }
}
