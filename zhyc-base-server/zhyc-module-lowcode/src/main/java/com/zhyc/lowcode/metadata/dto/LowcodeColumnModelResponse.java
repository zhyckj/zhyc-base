/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;

/**
 * 低代码字段模型响应。
 */
public class LowcodeColumnModelResponse {

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
   * 从领域模型创建响应。
   *
   * @param column 字段领域模型
   * @return 字段响应
   */
  public static LowcodeColumnModelResponse from(LowcodeColumnModel column) {
    LowcodeColumnModelResponse response = new LowcodeColumnModelResponse();
    response.code = column.getCode();
    response.name = column.getName();
    response.fieldType = column.getFieldType().name();
    response.length = column.getLength();
    response.scale = column.getScale();
    response.required = column.isRequired();
    response.primaryKey = column.isPrimaryKey();
    response.autoIncrement = column.isAutoIncrement();
    response.listVisible = column.isListVisible();
    response.formVisible = column.isFormVisible();
    response.queryable = column.isQueryable();
    response.dictCode = column.getDictCode();
    response.comment = column.getComment();
    return response;
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
   * 返回平台统一字段类型编码。
   *
   * @return 字段类型编码
   */
  public String getFieldType() {
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
   * 返回字段是否列表展示。
   *
   * @return 列表展示时返回 {@code true}
   */
  public boolean isListVisible() {
    return listVisible;
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
   * 返回字段是否可查询。
   *
   * @return 可查询时返回 {@code true}
   */
  public boolean isQueryable() {
    return queryable;
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
   * 返回字段备注。
   *
   * @return 字段备注
   */
  public String getComment() {
    return comment;
  }
}
