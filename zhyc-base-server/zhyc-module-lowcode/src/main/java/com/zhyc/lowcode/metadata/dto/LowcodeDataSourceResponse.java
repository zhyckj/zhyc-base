/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;

/**
 * 低代码数据源响应。
 */
public class LowcodeDataSourceResponse {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 数据源编码。 */
  private String code;
  /** 数据源名称。 */
  private String name;
  /** 数据库方言编码。 */
  private String dialect;
  /** JDBC 连接地址。 */
  private String jdbcUrl;
  /** 数据库登录用户名。 */
  private String username;
  /** 数据库口令密钥引用，仅用于编辑回显，不包含密钥明文。 */
  private String passwordSecretRef;
  /** 数据源是否启用。 */
  private boolean enabled;

  /**
   * 从领域模型创建响应。
   *
   * @param dataSource 数据源领域对象
   * @return 数据源响应
   */
  public static LowcodeDataSourceResponse from(LowcodeDataSource dataSource) {
    LowcodeDataSourceResponse response = new LowcodeDataSourceResponse();
    response.id = dataSource.getId();
    response.tenantId = dataSource.getTenantId();
    response.code = dataSource.getCode();
    response.name = dataSource.getName();
    response.dialect = dataSource.getDialect().getCode();
    response.jdbcUrl = dataSource.getJdbcUrl();
    response.username = dataSource.getUsername();
    response.passwordSecretRef = dataSource.getPasswordSecretRef();
    response.enabled = dataSource.isEnabled();
    return response;
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
   * 返回数据库方言编码。
   *
   * @return 数据库方言编码
   */
  public String getDialect() {
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
   * @return 数据库口令密钥引用，不包含密钥明文
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
}
