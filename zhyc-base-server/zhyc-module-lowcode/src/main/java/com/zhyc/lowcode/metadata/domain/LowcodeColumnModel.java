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
 * 低代码字段建模定义。
 */
public class LowcodeColumnModel {

  /** 字段编码，对应 Java 属性、接口字段和数据库列。 */
  private final String code;
  /** 字段名称，用于页面展示和注释。 */
  private final String name;
  /** 平台统一字段类型。 */
  private final LowcodeFieldType fieldType;
  /** 字段长度或数值精度。 */
  private final Integer length;
  /** 小数位数。 */
  private final Integer scale;
  /** 字段是否必填。 */
  private final boolean required;
  /** 字段是否主键。 */
  private final boolean primaryKey;
  /** 字段是否自增。 */
  private final boolean autoIncrement;
  /** 字段是否在列表页展示。 */
  private final boolean listVisible;
  /** 字段是否在表单页展示。 */
  private final boolean formVisible;
  /** 字段是否作为查询条件。 */
  private final boolean queryable;
  /** 绑定的系统字典编码，用于页面生成时渲染下拉、标签和枚举文案。 */
  private final String dictCode;
  /** 字段备注。 */
  private final String comment;

  private LowcodeColumnModel(Builder builder) {
    this.code = requireText(builder.code, "字段编码不能为空");
    this.name = requireText(builder.name, "字段名称不能为空");
    this.fieldType = Objects.requireNonNull(builder.fieldType, "字段类型不能为空");
    this.length = builder.length;
    this.scale = builder.scale;
    this.required = builder.required;
    this.primaryKey = builder.primaryKey;
    this.autoIncrement = builder.autoIncrement;
    this.listVisible = builder.listVisible;
    this.formVisible = builder.formVisible;
    this.queryable = builder.queryable;
    this.dictCode = normalizeNullableText(builder.dictCode);
    this.comment = builder.comment;
  }

  /**
   * 创建字段建模构建器。
   *
   * @param code 字段编码
   * @param name 字段名称
   * @param fieldType 平台统一字段类型
   * @return 字段建模构建器
   */
  public static Builder builder(String code, String name, LowcodeFieldType fieldType) {
    return new Builder(code, name, fieldType);
  }

  /**
   * 返回字段编码。
   *
   * @return 字段编码
   */
  public String getCode() {
    return code;
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
   * 返回字段长度或数值精度。
   *
   * @return 字段长度或数值精度
   */
  public Integer getLength() {
    return length;
  }

  /**
   * 返回小数位数。
   *
   * @return 小数位数
   */
  public Integer getScale() {
    return scale;
  }

  /**
   * 返回字段是否必填。
   *
   * @return 必填时返回 {@code true}
   */
  public boolean isRequired() {
    return required;
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
   * 返回字段是否在列表页展示。
   *
   * @return 列表页展示时返回 {@code true}
   */
  public boolean isListVisible() {
    return listVisible;
  }

  /**
   * 返回字段是否在表单页展示。
   *
   * @return 表单页展示时返回 {@code true}
   */
  public boolean isFormVisible() {
    return formVisible;
  }

  /**
   * 返回字段是否作为查询条件。
   *
   * @return 可查询时返回 {@code true}
   */
  public boolean isQueryable() {
    return queryable;
  }

  /**
   * 返回字段绑定的系统字典编码。
   *
   * @return 字典编码，未绑定时返回 {@code null}
   */
  public String getDictCode() {
    return dictCode;
  }

  /**
   * 返回字段备注。
   *
   * @return 字段备注，未配置时返回 {@code null}
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

  private static String normalizeNullableText(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return value.trim();
  }

  /**
   * 低代码字段建模构建器。
   */
  public static class Builder {

    /** 字段编码。 */
    private final String code;
    /** 字段名称。 */
    private final String name;
    /** 平台统一字段类型。 */
    private final LowcodeFieldType fieldType;
    /** 字段长度或数值精度。 */
    private Integer length;
    /** 小数位数。 */
    private Integer scale;
    /** 字段是否必填。 */
    private boolean required;
    /** 字段是否主键。 */
    private boolean primaryKey;
    /** 字段是否自增。 */
    private boolean autoIncrement;
    /** 字段是否在列表页展示。 */
    private boolean listVisible;
    /** 字段是否在表单页展示。 */
    private boolean formVisible;
    /** 字段是否作为查询条件。 */
    private boolean queryable;
    /** 绑定的系统字典编码。 */
    private String dictCode;
    /** 字段备注。 */
    private String comment;

    private Builder(String code, String name, LowcodeFieldType fieldType) {
      this.code = code;
      this.name = name;
      this.fieldType = fieldType;
    }

    /**
     * 设置字段长度或数值精度。
     *
     * @param length 字段长度或数值精度，必须大于 0
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
     * 设置字段是否必填。
     *
     * @param required 必填时传入 {@code true}
     * @return 当前构建器
     */
    public Builder required(boolean required) {
      this.required = required;
      return this;
    }

    /**
     * 设置字段是否主键。
     *
     * @param primaryKey 主键字段传入 {@code true}
     * @return 当前构建器
     */
    public Builder primaryKey(boolean primaryKey) {
      this.primaryKey = primaryKey;
      return this;
    }

    /**
     * 设置字段是否自增。
     *
     * @param autoIncrement 自增字段传入 {@code true}
     * @return 当前构建器
     */
    public Builder autoIncrement(boolean autoIncrement) {
      this.autoIncrement = autoIncrement;
      return this;
    }

    /**
     * 设置字段是否在列表页展示。
     *
     * @param listVisible 列表页展示时传入 {@code true}
     * @return 当前构建器
     */
    public Builder listVisible(boolean listVisible) {
      this.listVisible = listVisible;
      return this;
    }

    /**
     * 设置字段是否在表单页展示。
     *
     * @param formVisible 表单页展示时传入 {@code true}
     * @return 当前构建器
     */
    public Builder formVisible(boolean formVisible) {
      this.formVisible = formVisible;
      return this;
    }

    /**
     * 设置字段是否作为查询条件。
     *
     * @param queryable 可查询时传入 {@code true}
     * @return 当前构建器
     */
    public Builder queryable(boolean queryable) {
      this.queryable = queryable;
      return this;
    }

    /**
     * 设置字段绑定的系统字典编码。
     *
     * @param dictCode 字典编码，为空时表示不绑定字典
     * @return 当前构建器
     */
    public Builder dictCode(String dictCode) {
      this.dictCode = dictCode;
      return this;
    }

    /**
     * 设置字段备注。
     *
     * @param comment 字段备注
     * @return 当前构建器
     */
    public Builder comment(String comment) {
      this.comment = comment;
      return this;
    }

    /**
     * 构建低代码字段模型。
     *
     * @return 低代码字段模型
     */
    public LowcodeColumnModel build() {
      return new LowcodeColumnModel(this);
    }
  }
}
