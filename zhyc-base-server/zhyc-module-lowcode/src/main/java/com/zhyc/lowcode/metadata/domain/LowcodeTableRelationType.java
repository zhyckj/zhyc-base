/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

/**
 * 低代码表关系类型枚举。
 *
 * <p>用于约束主子表建模关系，避免生成器在联表查询、页面联动和移动端详情生成时遇到未定义关系。</p>
 */
public enum LowcodeTableRelationType {

  /** 一对多关系，常用于主表和明细子表生成。 */
  ONE_TO_MANY("ONE_TO_MANY", "一对多"),

  /** 一对一关系，常用于扩展表或详情表生成。 */
  ONE_TO_ONE("ONE_TO_ONE", "一对一");

  /** 持久化关系类型编码；对应表关系模型 relation_type 字段。 */
  private final String code;

  /** 关系类型中文说明；用于后台建模展示和生成说明。 */
  private final String description;

  /**
   * 创建低代码表关系类型枚举。
   *
   * @param code 持久化关系类型编码
   * @param description 关系类型中文说明
   */
  LowcodeTableRelationType(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 获取持久化关系类型编码。
   *
   * @return 表关系模型 relation_type 字段使用的关系类型编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 获取关系类型中文说明。
   *
   * @return 关系类型中文说明
   */
  public String getDescription() {
    return description;
  }

  /**
   * 根据持久化编码解析表关系类型。
   *
   * <p>只允许首期生成器支持的关系类型，避免未定义关系影响主子表代码生成。</p>
   *
   * @param code 持久化关系类型编码
   * @return 匹配的表关系类型
   */
  public static LowcodeTableRelationType fromCode(String code) {
    for (LowcodeTableRelationType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("表关系类型不支持: " + code);
  }
}
