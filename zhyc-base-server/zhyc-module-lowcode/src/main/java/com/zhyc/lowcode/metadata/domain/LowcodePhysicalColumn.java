/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

import com.zhyc.lowcode.db.LowcodeFieldType;
import java.util.Objects;

/**
 * 数据源物理表字段结构。
 *
 * <p>用于把数据库元数据读取结果转换为低代码字段模型，字段中不包含任何数据行内容或敏感值。</p>
 */
public class LowcodePhysicalColumn {

  /** 物理字段名。 */
  private final String name;
  /** 平台统一字段类型。 */
  private final LowcodeFieldType fieldType;
  /** 字段长度。 */
  private final Integer length;
  /** 小数位数。 */
  private final Integer scale;
  /** 是否允许为空。 */
  private final boolean nullable;
  /** 是否主键。 */
  private final boolean primaryKey;
  /** 是否自增。 */
  private final boolean autoIncrement;
  /** 字段注释。 */
  private final String comment;

  private LowcodePhysicalColumn(Builder builder) {
    this.name = requireText(builder.name, "物理字段名不能为空");
    this.fieldType = Objects.requireNonNull(builder.fieldType, "字段类型不能为空");
    this.length = builder.length;
    this.scale = builder.scale;
    this.nullable = builder.nullable;
    this.primaryKey = builder.primaryKey;
    this.autoIncrement = builder.autoIncrement;
    this.comment = trimToNull(builder.comment);
  }

  /**
   * 创建物理字段构建器。
   *
   * @param name 物理字段名
   * @param fieldType 平台统一字段类型
   * @return 物理字段构建器
   */
  public static Builder builder(String name, LowcodeFieldType fieldType) {
    return new Builder(name, fieldType);
  }

  /**
   * 返回物理字段名。
   *
   * @return 物理字段名
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
   * 返回字段长度。
   *
   * @return 字段长度，未读取到时返回 {@code null}
   */
  public Integer getLength() {
    return length;
  }

  /**
   * 返回小数位数。
   *
   * @return 小数位数，未读取到时返回 {@code null}
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
   * 返回字段是否主键。
   *
   * @return 主键字段返回 {@code true}
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
   * @return 字段注释，未读取到时返回 {@code null}
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

  private static String trimToNull(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return value.trim();
  }

  /**
   * 数据源物理字段构建器。
   */
  public static class Builder {

    /** 物理字段名。 */
    private final String name;
    /** 平台统一字段类型。 */
    private final LowcodeFieldType fieldType;
    /** 字段长度。 */
    private Integer length;
    /** 小数位数。 */
    private Integer scale;
    /** 是否允许为空，默认允许为空。 */
    private boolean nullable = true;
    /** 是否主键。 */
    private boolean primaryKey;
    /** 是否自增。 */
    private boolean autoIncrement;
    /** 字段注释。 */
    private String comment;

    private Builder(String name, LowcodeFieldType fieldType) {
      this.name = name;
      this.fieldType = fieldType;
    }

    /**
     * 设置字段长度。
     *
     * @param length 字段长度
     * @return 当前构建器
     */
    public Builder length(Integer length) {
      this.length = length;
      return this;
    }

    /**
     * 设置小数位数。
     *
     * @param scale 小数位数
     * @return 当前构建器
     */
    public Builder scale(Integer scale) {
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
     * 设置字段是否主键。
     *
     * @param primaryKey 主键时传入 {@code true}
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
     * 构建物理字段。
     *
     * @return 物理字段
     */
    public LowcodePhysicalColumn build() {
      return new LowcodePhysicalColumn(this);
    }
  }
}
