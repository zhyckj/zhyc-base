/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

import java.util.Arrays;

/**
 * 低代码页面布局类型。
 *
 * <p>用于约束页面模型的布局模板，保证后台管理端和 uni-app 生成器只消费已登记的布局编码。</p>
 */
public enum LowcodePageLayoutType {

  /** 后台管理列表页表格布局。 */
  TABLE("TABLE", "后台管理列表页表格布局"),
  /** 后台管理新增和编辑表单布局。 */
  FORM("FORM", "后台管理表单布局"),
  /** 后台管理详情页描述列表布局。 */
  DESCRIPTIONS("DESCRIPTIONS", "后台管理详情布局"),
  /** uni-app 移动端页面布局。 */
  UNIAPP_PAGE("UNIAPP_PAGE", "uni-app 移动页面布局");

  /** 布局类型编码，作为数据库持久化值和生成模板契约值。 */
  private final String code;
  /** 布局类型中文说明，用于后台展示和生成配置说明。 */
  private final String description;

  LowcodePageLayoutType(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 返回布局类型编码。
   *
   * @return 布局类型编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 返回布局类型中文说明。
   *
   * @return 布局类型中文说明
   */
  public String getDescription() {
    return description;
  }

  /**
   * 根据持久化编码解析布局类型。
   *
   * @param code 布局类型编码
   * @return 匹配的布局类型
   */
  public static LowcodePageLayoutType fromCode(String code) {
    return Arrays.stream(values())
        .filter(type -> type.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("页面布局类型不支持: " + code));
  }
}
