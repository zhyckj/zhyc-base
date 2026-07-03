/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;

/**
 * 低代码数据源保存请求。
 */
public class LowcodeDataSourceSaveRequest {

  /** 租户业务编码。 */
  private String tenantId;
  /** 数据源编码，同一租户内唯一。 */
  private String code;
  /** 数据源名称。 */
  private String name;
  /** 数据库方言编码。 */
  private String dialect;
  /** JDBC 连接地址。 */
  private String jdbcUrl;
  /** 数据库登录用户名。 */
  private String username;
  /** 数据库口令密钥引用，不接收明文口令。 */
  private String passwordSecretRef;
  /** 数据源是否启用。 */
  private boolean enabled = true;

  /**
   * 转换成领域模型。
   *
   * @return 低代码数据源领域对象
   */
  public LowcodeDataSource toDomain() {
    return new LowcodeDataSource(null, tenantId, code, name,
        LowcodeDatabaseDialect.fromCode(dialect), jdbcUrl, username, passwordSecretRef, enabled);
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
   * 设置租户业务编码。
   *
   * @param tenantId 租户业务编码
   */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
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
   * 设置数据源编码。
   *
   * @param code 数据源编码
   */
  public void setCode(String code) {
    this.code = code;
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
   * 设置数据源名称。
   *
   * @param name 数据源名称
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * 返回数据库方言编码。
   *
   * @return 数据库方言编码
   */
  public String getDialect() {
    return dialect;
  }

  /**
   * 设置数据库方言编码。
   *
   * @param dialect 数据库方言编码
   */
  public void setDialect(String dialect) {
    this.dialect = dialect;
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
   * 设置 JDBC 连接地址。
   *
   * @param jdbcUrl JDBC 连接地址
   */
  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
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
   * 设置数据库登录用户名。
   *
   * @param username 数据库登录用户名
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * 返回数据库口令密钥引用。
   *
   * @return 密钥引用
   */
  public String getPasswordSecretRef() {
    return passwordSecretRef;
  }

  /**
   * 设置数据库口令密钥引用。
   *
   * @param passwordSecretRef 密钥引用
   */
  public void setPasswordSecretRef(String passwordSecretRef) {
    this.passwordSecretRef = passwordSecretRef;
  }

  /**
   * 返回数据源是否启用。
   *
   * @return 启用时返回 {@code true}
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * 设置数据源是否启用。
   *
   * @param enabled 启用标记
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
