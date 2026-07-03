/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

import java.util.Objects;

/**
 * 低代码数据源定义。
 *
 * <p>保存在线数据源管理需要的基础元数据；敏感密码不进入该领域对象，后续由密钥组件托管。</p>
 */
public class LowcodeDataSource {

  /** 数据库主键。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 数据源编码，同一租户内唯一。 */
  private final String code;
  /** 数据源名称。 */
  private final String name;
  /** 数据库类型。 */
  private final LowcodeDatabaseDialect dialect;
  /** JDBC 连接地址。 */
  private final String jdbcUrl;
  /** 数据库登录用户名。 */
  private final String username;
  /** 数据库口令密钥引用，用于定位密钥托管组件中的密文。 */
  private final String passwordSecretRef;
  /** 数据源是否启用。 */
  private final boolean enabled;

  /**
   * 创建低代码数据源定义。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param code 数据源编码，同一租户内唯一
   * @param name 数据源名称
   * @param dialect 数据库类型
   * @param jdbcUrl JDBC 连接地址
   * @param username 数据库登录用户名
   * @param enabled 数据源是否启用
   */
  public LowcodeDataSource(Long id, String tenantId, String code, String name,
                           LowcodeDatabaseDialect dialect, String jdbcUrl, String username,
                           boolean enabled) {
    this(id, tenantId, code, name, dialect, jdbcUrl, username, null, enabled);
  }

  /**
   * 创建低代码数据源定义。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param code 数据源编码，同一租户内唯一
   * @param name 数据源名称
   * @param dialect 数据库类型
   * @param jdbcUrl JDBC 连接地址
   * @param username 数据库登录用户名
   * @param passwordSecretRef 数据库口令密钥引用，不保存明文密码
   * @param enabled 数据源是否启用
   */
  public LowcodeDataSource(Long id, String tenantId, String code, String name,
                           LowcodeDatabaseDialect dialect, String jdbcUrl, String username,
                           String passwordSecretRef, boolean enabled) {
    this.id = id;
    this.tenantId = requireText(tenantId, "租户业务编码不能为空");
    this.code = requireText(code, "数据源编码不能为空");
    this.name = requireText(name, "数据源名称不能为空");
    this.dialect = Objects.requireNonNull(dialect, "数据库类型不能为空");
    this.jdbcUrl = requireText(jdbcUrl, "JDBC 连接地址不能为空");
    this.username = requireText(username, "数据库用户名不能为空");
    this.passwordSecretRef = trimToNull(passwordSecretRef);
    this.enabled = enabled;
  }

  /**
   * 返回数据库主键。
   *
   * @return 数据库主键
   */
  public Long getId() {
    return id;
  }

  /**
   * 返回租户业务编码。
   *
   * @return 租户业务编码
   */
  public String getTenantId() {
    return tenantId;
  }

  /**
   * 返回数据源编码。
   *
   * @return 数据源编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 返回数据源名称。
   *
   * @return 数据源名称
   */
  public String getName() {
    return name;
  }

  /**
   * 返回数据库类型。
   *
   * @return 数据库类型
   */
  public LowcodeDatabaseDialect getDialect() {
    return dialect;
  }

  /**
   * 返回 JDBC 连接地址。
   *
   * @return JDBC 连接地址
   */
  public String getJdbcUrl() {
    return jdbcUrl;
  }

  /**
   * 返回数据库登录用户名。
   *
   * @return 数据库登录用户名
   */
  public String getUsername() {
    return username;
  }

  /**
   * 返回数据库口令密钥引用。
   *
   * @return 密钥引用，未配置时返回 {@code null}
   */
  public String getPasswordSecretRef() {
    return passwordSecretRef;
  }

  /**
   * 返回数据源是否启用。
   *
   * @return 启用时返回 {@code true}
   */
  public boolean isEnabled() {
    return enabled;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  private static String trimToNull(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return value.trim();
  }
}
