/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.security;

import java.util.List;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 基于核心平台用户表的认证中心用户查询服务。
 *
 * <p>认证中心登录页和后台用户管理共用 `sys_user.password_hash`，后台重置密码后无需再同步
 * `ZHYC_AUTH_USER_PASSWORD` 配置。</p>
 */
public class JdbcPlatformUserDetailsService implements UserDetailsService {

  /** 启用状态编码。 */
  private static final String STATUS_ENABLED = "enabled";
  /** 查询平台用户登录信息 SQL。 */
  private static final String QUERY_USER_SQL = """
      SELECT username, password_hash, status
      FROM sys_user
      WHERE tenant_id = ? AND username = ?
      LIMIT 1
      """;

  /** JDBC 操作对象。 */
  private final JdbcOperations jdbcOperations;
  /** 平台租户业务编码。 */
  private final String tenantId;

  /**
   * 创建平台用户查询服务。
   *
   * @param jdbcOperations JDBC 操作对象
   * @param tenantId 平台租户业务编码
   */
  public JdbcPlatformUserDetailsService(JdbcOperations jdbcOperations, String tenantId) {
    this.jdbcOperations = Objects.requireNonNull(jdbcOperations, "JDBC 操作对象不能为空");
    this.tenantId = requireText(tenantId, "平台租户业务编码不能为空");
  }

  /**
   * 按登录账号查询平台用户。
   *
   * @param username 登录账号
   * @return Spring Security 用户信息
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    String normalizedUsername = requireText(username, "登录账号不能为空");
    List<UserDetails> users = jdbcOperations.query(
        QUERY_USER_SQL,
        (rs, rowNum) -> User.withUsername(rs.getString("username"))
            .password(requireText(rs.getString("password_hash"), "用户密码哈希不能为空"))
            .disabled(!STATUS_ENABLED.equals(rs.getString("status")))
            .authorities(AuthorityUtils.createAuthorityList("ROLE_AUTH_USER"))
            .build(),
        tenantId,
        normalizedUsername);
    if (users.isEmpty()) {
      throw new UsernameNotFoundException("平台用户不存在：" + normalizedUsername);
    }
    return users.get(0);
  }

  /**
   * 校验并清理文本。
   *
   * @param value 文本值
   * @param message 错误消息
   * @return 清理后的文本
   */
  private static String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }
}
