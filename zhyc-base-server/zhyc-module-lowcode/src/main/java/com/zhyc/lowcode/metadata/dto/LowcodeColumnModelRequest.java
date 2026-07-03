/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;

/**
 * 低代码字段模型保存请求。
 */
public class LowcodeColumnModelRequest {

  /** 字段编码。 */
  private String code;
  /** 字段名称。 */
  private String name;
  /** 平台统一字段类型编码。 */
  private String fieldType;
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
  /** 字段是否列表展示。 */
  private boolean listVisible;
  /** 字段是否表单展示。 */
  private boolean formVisible;
  /** 字段是否可查询。 */
  private boolean queryable;
  /** 绑定的系统字典编码。 */
  private String dictCode;
  /** 字段备注。 */
  private String comment;

  /**
   * 转换成字段领域模型。
   *
   * @return 字段领域模型
   */
  public LowcodeColumnModel toDomain() {
    return LowcodeColumnModel.builder(code, name, LowcodeFieldType.valueOf(fieldType))
        .length(length)
        .scale(scale)
        .required(required)
        .primaryKey(primaryKey)
        .autoIncrement(autoIncrement)
        .listVisible(listVisible)
        .formVisible(formVisible)
        .queryable(queryable)
        .dictCode(dictCode)
        .comment(comment)
        .build();
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
   * 设置字段编码。
   *
   * @param code 字段编码
   */
  public void setCode(String code) {
    this.code = code;
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
   * 设置字段名称。
   *
   * @param name 字段名称
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * 返回平台统一字段类型编码。
   *
   * @return 字段类型编码
   */
  public String getFieldType() {
    return fieldType;
  }

  /**
   * 设置平台统一字段类型编码。
   *
   * @param fieldType 字段类型编码
   */
  public void setFieldType(String fieldType) {
    this.fieldType = fieldType;
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
   * 设置字段长度或数值精度。
   *
   * @param length 字段长度或数值精度
   */
  public void setLength(Integer length) {
    this.length = length;
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
   * 设置小数位数。
   *
   * @param scale 小数位数
   */
  public void setScale(Integer scale) {
    this.scale = scale;
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
   * 设置字段是否必填。
   *
   * @param required 必填标记
   */
  public void setRequired(boolean required) {
    this.required = required;
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
   * 设置字段是否主键。
   *
   * @param primaryKey 主键标记
   */
  public void setPrimaryKey(boolean primaryKey) {
    this.primaryKey = primaryKey;
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
   * 设置字段是否自增。
   *
   * @param autoIncrement 自增标记
   */
  public void setAutoIncrement(boolean autoIncrement) {
    this.autoIncrement = autoIncrement;
  }

  /**
   * 返回字段是否列表展示。
   *
   * @return 列表展示时返回 {@code true}
   */
  public boolean isListVisible() {
    return listVisible;
  }

  /**
   * 设置字段是否列表展示。
   *
   * @param listVisible 列表展示标记
   */
  public void setListVisible(boolean listVisible) {
    this.listVisible = listVisible;
  }

  /**
   * 返回字段是否表单展示。
   *
   * @return 表单展示时返回 {@code true}
   */
  public boolean isFormVisible() {
    return formVisible;
  }

  /**
   * 设置字段是否表单展示。
   *
   * @param formVisible 表单展示标记
   */
  public void setFormVisible(boolean formVisible) {
    this.formVisible = formVisible;
  }

  /**
   * 返回字段是否可查询。
   *
   * @return 可查询时返回 {@code true}
   */
  public boolean isQueryable() {
    return queryable;
  }

  /**
   * 设置字段是否可查询。
   *
   * @param queryable 可查询标记
   */
  public void setQueryable(boolean queryable) {
    this.queryable = queryable;
  }

  /**
   * 返回绑定的系统字典编码。
   *
   * @return 字典编码，未绑定时返回 {@code null}
   */
  public String getDictCode() {
    return dictCode;
  }

  /**
   * 设置绑定的系统字典编码。
   *
   * @param dictCode 字典编码，空值表示不绑定字典
   */
  public void setDictCode(String dictCode) {
    this.dictCode = dictCode;
  }

  /**
   * 返回字段备注。
   *
   * @return 字段备注
   */
  public String getComment() {
    return comment;
  }

  /**
   * 设置字段备注。
   *
   * @param comment 字段备注
   */
  public void setComment(String comment) {
    this.comment = comment;
  }
}
