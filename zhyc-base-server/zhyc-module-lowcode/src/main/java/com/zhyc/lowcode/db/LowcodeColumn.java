/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import java.util.Objects;

/**
 * 低代码数据表字段模型。
 */
public class LowcodeColumn {

  /** 字段名称，对应数据库列名。 */
  private final String name;
  /** 平台统一字段类型。 */
  private final LowcodeFieldType fieldType;
  /** 字段长度或精度，按字段类型解释。 */
  private final Integer length;
  /** 小数位数，仅小数字段使用。 */
  private final Integer scale;
  /** 字段是否允许为空。 */
  private final boolean nullable;
  /** 字段是否为主键。 */
  private final boolean primaryKey;
  /** 字段是否自增。 */
  private final boolean autoIncrement;
  /** 字段注释。 */
  private final String comment;

  private LowcodeColumn(Builder builder) {
    this.name = requireText(builder.name, "字段名称不能为空");
    this.fieldType = Objects.requireNonNull(builder.fieldType, "字段类型不能为空");
    this.length = builder.length;
    this.scale = builder.scale;
    this.nullable = builder.nullable;
    this.primaryKey = builder.primaryKey;
    this.autoIncrement = builder.autoIncrement;
    this.comment = builder.comment;
  }

  /**
   * 创建字段模型构建器。
   *
   * @param name 字段名称
   * @param fieldType 平台统一字段类型
   * @return 字段模型构建器
   */
  public static Builder builder(String name, LowcodeFieldType fieldType) {
    return new Builder(name, fieldType);
  }

  /**
   * 返回字段名称。
   *
   * @return 字段名称
   */
  public String getName() {
    return name;
  }

  /**
   * 返回平台统一字段类型。
   *
   * @return 平台统一字段类型
   */
  public LowcodeFieldType getFieldType() {
    return fieldType;
  }

  /**
   * 返回字段长度或精度。
   *
   * @return 字段长度或精度，未配置时返回 {@code null}
   */
  public Integer getLength() {
    return length;
  }

  /**
   * 返回小数位数。
   *
   * @return 小数位数，未配置时返回 {@code null}
   */
  public Integer getScale() {
    return scale;
  }

  /**
   * 返回字段是否允许为空。
   *
   * @return 允许为空时返回 {@code true}
   */
  public boolean isNullable() {
    return nullable;
  }

  /**
   * 返回字段是否为主键。
   *
   * @return 是主键时返回 {@code true}
   */
  public boolean isPrimaryKey() {
    return primaryKey;
  }

  /**
   * 返回字段是否自增。
   *
   * @return 自增字段返回 {@code true}
   */
  public boolean isAutoIncrement() {
    return autoIncrement;
  }

  /**
   * 返回字段注释。
   *
   * @return 字段注释，未配置时返回 {@code null}
   */
  public String getComment() {
    return comment;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  /**
   * 低代码字段模型构建器。
   */
  public static class Builder {

    /** 字段名称。 */
    private final String name;
    /** 平台统一字段类型。 */
    private final LowcodeFieldType fieldType;
    /** 字段长度或精度。 */
    private Integer length;
    /** 小数位数。 */
    private Integer scale;
    /** 字段是否允许为空，默认允许为空。 */
    private boolean nullable = true;
    /** 字段是否为主键。 */
    private boolean primaryKey;
    /** 字段是否自增。 */
    private boolean autoIncrement;
    /** 字段注释。 */
    private String comment;

    private Builder(String name, LowcodeFieldType fieldType) {
      this.name = name;
      this.fieldType = fieldType;
    }

    /**
     * 设置字段长度或精度。
     *
     * @param length 字段长度或精度，必须大于 0
     * @return 当前构建器
     */
    public Builder length(Integer length) {
      if (length != null && length <= 0) {
        throw new IllegalArgumentException("字段长度必须大于 0");
      }
      this.length = length;
      return this;
    }

    /**
     * 设置小数位数。
     *
     * @param scale 小数位数，必须大于等于 0
     * @return 当前构建器
     */
    public Builder scale(Integer scale) {
      if (scale != null && scale < 0) {
        throw new IllegalArgumentException("小数位数不能小于 0");
      }
      this.scale = scale;
      return this;
    }

    /**
     * 设置字段是否允许为空。
     *
     * @param nullable 允许为空时传入 {@code true}
     * @return 当前构建器
     */
    public Builder nullable(boolean nullable) {
      this.nullable = nullable;
      return this;
    }

    /**
     * 设置字段是否为主键。
     *
     * @param primaryKey 是主键时传入 {@code true}
     * @return 当前构建器
     */
    public Builder primaryKey(boolean primaryKey) {
      this.primaryKey = primaryKey;
      return this;
    }

    /**
     * 设置字段是否自增。
     *
     * @param autoIncrement 自增时传入 {@code true}
     * @return 当前构建器
     */
    public Builder autoIncrement(boolean autoIncrement) {
      this.autoIncrement = autoIncrement;
      return this;
    }

    /**
     * 设置字段注释。
     *
     * @param comment 字段注释
     * @return 当前构建器
     */
    public Builder comment(String comment) {
      this.comment = comment;
      return this;
    }

    /**
     * 构建字段模型。
     *
     * @return 字段模型
     */
    public LowcodeColumn build() {
      return new LowcodeColumn(this);
    }
  }
}
