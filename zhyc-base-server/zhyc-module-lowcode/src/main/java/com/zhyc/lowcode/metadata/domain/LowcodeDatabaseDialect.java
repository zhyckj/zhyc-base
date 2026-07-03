/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

/**
 * 低代码数据源数据库类型。
 *
 * <p>首期默认落地 MySQL，同时内置 PostgreSQL、Oracle、SQL Server、达梦数据库的低代码建表与分页兼容能力。</p>
 */
public enum LowcodeDatabaseDialect {

  /** MySQL 数据库。 */
  MYSQL("mysql", "MySQL"),
  /** PostgreSQL 数据库。 */
  POSTGRESQL("postgresql", "PostgreSQL"),
  /** Oracle 数据库。 */
  ORACLE("oracle", "Oracle"),
  /** SQL Server 数据库。 */
  SQLSERVER("sqlserver", "SQL Server"),
  /** 达梦数据库。 */
  DM("dm", "达梦数据库");

  /** 数据库类型编码。 */
  private final String code;
  /** 数据库类型显示名称。 */
  private final String label;

  /**
   * 创建数据库类型枚举。
   *
   * @param code 数据库类型编码
   * @param label 数据库类型显示名称
   */
  LowcodeDatabaseDialect(String code, String label) {
    this.code = code;
    this.label = label;
  }

  /**
   * 返回数据库类型编码。
   *
   * @return 数据库类型编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 返回数据库类型显示名称。
   *
   * @return 数据库类型显示名称
   */
  public String getLabel() {
    return label;
  }

  /**
   * 根据数据库方言编码解析枚举。
   *
   * <p>用于数据源配置、低代码生成请求和方言注册中心之间统一识别数据库类型；未知编码会明确拒绝，
   * 避免生成器落入错误的 DDL 或分页实现。</p>
   *
   * @param code 数据库方言编码，允许前后存在空白
   * @return 数据库方言枚举
   */
  public static LowcodeDatabaseDialect fromCode(String code) {
    String normalizedCode = code == null ? "" : code.trim();
    for (LowcodeDatabaseDialect dialect : values()) {
      if (dialect.code.equalsIgnoreCase(normalizedCode)) {
        return dialect;
      }
    }
    throw new IllegalArgumentException("不支持的低代码数据库方言编码: " + code);
  }
}
