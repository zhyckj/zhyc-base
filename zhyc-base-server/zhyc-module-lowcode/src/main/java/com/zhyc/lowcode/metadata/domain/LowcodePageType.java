/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

import java.util.Arrays;

/**
 * 低代码页面类型。
 *
 * <p>用于约束页面模型的目标端和模板类型，避免低代码生成器接收未登记的页面类型。</p>
 */
public enum LowcodePageType {

  /** 后台管理列表页，用于生成 Vue 表格页面。 */
  LIST("LIST", "后台管理列表页"),
  /** 后台管理表单页，用于生成 Vue 新增和编辑页面。 */
  FORM("FORM", "后台管理表单页"),
  /** 后台管理详情页，用于生成 Vue 详情展示页面。 */
  DETAIL("DETAIL", "后台管理详情页"),
  /** uni-app 移动列表页，用于生成移动端列表页面。 */
  MOBILE("MOBILE", "uni-app 移动列表页"),
  /** uni-app 移动表单页，用于生成移动端新增和编辑页面。 */
  MOBILE_FORM("MOBILE_FORM", "uni-app 移动表单页"),
  /** uni-app 移动详情页，用于生成移动端详情页面。 */
  MOBILE_DETAIL("MOBILE_DETAIL", "uni-app 移动详情页");

  /** 页面类型编码，作为数据库持久化值和前后端契约值。 */
  private final String code;
  /** 页面类型中文说明，用于后台展示和生成模板说明。 */
  private final String description;

  LowcodePageType(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 返回页面类型编码。
   *
   * @return 页面类型编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 返回页面类型中文说明。
   *
   * @return 页面类型中文说明
   */
  public String getDescription() {
    return description;
  }

  /**
   * 根据持久化编码解析页面类型。
   *
   * @param code 页面类型编码
   * @return 匹配的页面类型
   */
  public static LowcodePageType fromCode(String code) {
    return Arrays.stream(values())
        .filter(type -> type.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("页面类型不支持: " + code));
  }
}
